package com.rbkmoney.newway.dao.party.impl;

import com.rbkmoney.dao.impl.AbstractGenericDao;
import com.rbkmoney.mapper.RecordRowMapper;
import com.rbkmoney.newway.dao.party.iface.ContractorDao;
import com.rbkmoney.newway.domain.tables.pojos.Contractor;
import com.rbkmoney.newway.domain.tables.records.ContractorRecord;
import com.rbkmoney.newway.exception.DaoException;
import com.rbkmoney.newway.exception.NotFoundException;
import org.jooq.Query;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.rbkmoney.newway.domain.Tables.*;
import static com.rbkmoney.newway.domain.Tables.SHOP;

@Component
public class ContractorDaoImpl extends AbstractGenericDao implements ContractorDao {

    private final RowMapper<Contractor> contractorRowMapper;

    public ContractorDaoImpl(DataSource dataSource) {
        super(dataSource);
        contractorRowMapper = new RecordRowMapper<>(CONTRACTOR, Contractor.class);
    }

    @Override
    public Optional<Long> save(Contractor contractor) throws DaoException {
        ContractorRecord record = getDslContext().newRecord(CONTRACTOR, contractor);
        Query query = getDslContext().insertInto(CONTRACTOR).set(record)
                .onConflict(CONTRACTOR.PARTY_ID, CONTRACTOR.SEQUENCE_ID, CONTRACTOR.CHANGE_ID,
                        CONTRACTOR.CLAIM_EFFECT_ID, CONTRACTOR.REVISION)
                .doNothing()
                .returning(CONTRACTOR.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        execute(query, keyHolder);
        return Optional.ofNullable(keyHolder.getKey()).map(Number::longValue);
    }

    @Override
    public void saveBatch(List<Contractor> contractors) throws DaoException {
        List<Query> queries = contractors.stream()
                .map(contractor -> getDslContext().newRecord(CONTRACTOR, contractor))
                .map(contractorRecord -> getDslContext().insertInto(CONTRACTOR)
                        .set(contractorRecord)
                        .onConflict(CONTRACTOR.PARTY_ID, CONTRACTOR.SEQUENCE_ID, CONTRACTOR.CHANGE_ID, CONTRACTOR.CLAIM_EFFECT_ID, CONTRACTOR.REVISION)
                        .doNothing()
                )
                .collect(Collectors.toList());
        batchExecute(queries);
    }

    @Override
    public Contractor get(String partyId, String contractorId) throws DaoException {
        Query query = getDslContext().selectFrom(CONTRACTOR)
                .where(CONTRACTOR.PARTY_ID.eq(partyId).and(CONTRACTOR.CONTRACTOR_ID.eq(contractorId)).and(CONTRACTOR.CURRENT));
        Contractor contractor = fetchOne(query, contractorRowMapper);
        if (contractor == null) {
            throw new NotFoundException(String.format("Contractor not found, contractorId='%s'", contractorId));
        }
        return contractor;
    }

    @Override
    public void updateNotCurrent(Long id) throws DaoException {
        Query query = getDslContext().update(CONTRACTOR).set(CONTRACTOR.CURRENT, false)
                .where(CONTRACTOR.ID.eq(id).and(CONTRACTOR.CURRENT));
        executeOne(query);
    }

    @Override
    public void updateNotCurrent(List<Long> ids) throws DaoException {
        Query query = getDslContext().update(CONTRACTOR).set(CONTRACTOR.CURRENT, false).where(CONTRACTOR.ID.in(ids));
        execute(query);
    }

    @Override
    public void switchCurrent(List<Long> ids) throws DaoException {
        ids.forEach(id ->
                this.getNamedParameterJdbcTemplate().update("update nw.contractor set current = false where id =:id and current;" +
                                "update nw.contractor set current = true where id = (select max(id) from nw.contractor where id =:id);",
                        new MapSqlParameterSource("id", id)));
    }

    @Override
    public List<Contractor> getByPartyId(String partyId) {
        Query query = getDslContext().selectFrom(CONTRACTOR)
                .where(CONTRACTOR.PARTY_ID.eq(partyId).and(CONTRACTOR.CURRENT));
        return fetch(query, contractorRowMapper);
    }
}
