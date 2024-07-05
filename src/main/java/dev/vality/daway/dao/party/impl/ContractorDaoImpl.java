package dev.vality.daway.dao.party.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.party.iface.ContractorDao;
import dev.vality.daway.domain.tables.pojos.Contractor;
import dev.vality.daway.domain.tables.records.ContractorRecord;
import dev.vality.daway.exception.DaoException;
import dev.vality.daway.exception.NotFoundException;
import dev.vality.mapper.RecordRowMapper;
import org.jooq.Query;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Optional;

import static dev.vality.daway.domain.Tables.CONTRACTOR;

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
                .onConflict(CONTRACTOR.PARTY_ID, CONTRACTOR.CONTRACTOR_ID, CONTRACTOR.SEQUENCE_ID, CONTRACTOR.CHANGE_ID,
                        CONTRACTOR.CLAIM_EFFECT_ID)
                .doNothing()
                .returning(CONTRACTOR.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        execute(query, keyHolder);
        return Optional.ofNullable(keyHolder.getKey()).map(Number::longValue);
    }

    @Override
    public Contractor get(String partyId, String contractorId) throws DaoException {
        Query query = getDslContext().selectFrom(CONTRACTOR)
                .where(CONTRACTOR.PARTY_ID.eq(partyId).and(CONTRACTOR.CONTRACTOR_ID.eq(contractorId))
                        .and(CONTRACTOR.CURRENT));
        return Optional.ofNullable(fetchOne(query, contractorRowMapper))
                .orElseThrow(() -> new NotFoundException(
                        String.format("Contractor not found, contractorId='%s'", contractorId)));
    }

    @Override
    public void updateNotCurrent(Long id) throws DaoException {
        Query query = getDslContext().update(CONTRACTOR).set(CONTRACTOR.CURRENT, false)
                .where(CONTRACTOR.ID.eq(id).and(CONTRACTOR.CURRENT));
        executeOne(query);
    }
}
