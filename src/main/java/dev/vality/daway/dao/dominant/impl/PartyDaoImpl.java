package dev.vality.daway.dao.dominant.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.dominant.iface.DomainObjectDao;
import dev.vality.daway.domain.tables.pojos.Party;
import dev.vality.daway.domain.tables.records.PartyRecord;
import dev.vality.daway.exception.DaoException;
import dev.vality.daway.exception.NotFoundException;
import dev.vality.mapper.RecordRowMapper;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Query;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Optional;

import static dev.vality.daway.domain.Tables.PARTY;

@Slf4j
@Component
public class PartyDaoImpl extends AbstractGenericDao implements DomainObjectDao<Party, String> {

    private final RowMapper<Party> rowMapper;

    public PartyDaoImpl(DataSource dataSource) {
        super(dataSource);
        rowMapper = new RecordRowMapper<>(PARTY, Party.class);
    }

    @Override
    public Long save(Party party) throws DaoException {
        PartyRecord record = getDslContext().newRecord(PARTY, party);
        Query query = getDslContext().insertInto(PARTY).set(record)
                .onConflict(PARTY.PARTY_ID, PARTY.DOMINANT_VERSION_ID)
                .doNothing()
                .returning(PARTY.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        execute(query, keyHolder);
        return Optional.ofNullable(keyHolder.getKey()).map(Number::longValue).orElse(null);
    }

    @Override
    public void updateNotCurrent(String id) throws DaoException {
        Query query = getDslContext().update(PARTY).set(PARTY.CURRENT, false)
                .where(PARTY.PARTY_ID.eq(id));
        execute(query);
    }

    public Party get(String partyId) throws DaoException {
        Query query = getDslContext().selectFrom(PARTY)
                .where(PARTY.PARTY_ID.eq(partyId).and(PARTY.CURRENT));

        Party party = fetchOne(query, rowMapper);

        if (party == null) {
            throw new NotFoundException(String.format("Party not found, partyId='%s'", partyId));
        }
        return party;
    }
}
