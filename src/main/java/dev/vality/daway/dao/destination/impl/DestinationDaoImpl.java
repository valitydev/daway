package dev.vality.daway.dao.destination.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.destination.iface.DestinationDao;
import dev.vality.daway.domain.tables.pojos.Destination;
import dev.vality.daway.domain.tables.records.DestinationRecord;
import dev.vality.daway.exception.DaoException;
import dev.vality.daway.exception.NotFoundException;
import dev.vality.mapper.RecordRowMapper;
import org.jooq.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Optional;

import static dev.vality.daway.domain.tables.Destination.DESTINATION;

@Component
public class DestinationDaoImpl extends AbstractGenericDao implements DestinationDao {

    private final RowMapper<Destination> destinationRowMapper;

    @Autowired
    public DestinationDaoImpl(DataSource dataSource) {
        super(dataSource);
        destinationRowMapper = new RecordRowMapper<>(DESTINATION, Destination.class);
    }

    @Override
    public Optional<Long> save(Destination destination) throws DaoException {
        DestinationRecord record = getDslContext().newRecord(DESTINATION, destination);
        Query query = getDslContext()
                .insertInto(DESTINATION)
                .set(record)
                .onConflict(DESTINATION.DESTINATION_ID, DESTINATION.SEQUENCE_ID)
                .doNothing()
                .returning(DESTINATION.ID);

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        execute(query, keyHolder);
        return Optional.ofNullable(keyHolder.getKey()).map(Number::longValue);
    }

    @Override
    public Destination get(String destinationId) throws DaoException {
        Query query = getDslContext().selectFrom(DESTINATION)
                .where(DESTINATION.DESTINATION_ID.eq(destinationId)
                        .and(DESTINATION.CURRENT));

        return Optional.ofNullable(fetchOne(query, destinationRowMapper))
                .orElseThrow(() -> new NotFoundException(
                        String.format("Destination not found, destinationId='%s'", destinationId)));
    }

    @Override
    public void updateNotCurrent(Long destinationId) throws DaoException {
        Query query = getDslContext().update(DESTINATION).set(DESTINATION.CURRENT, false)
                .where(DESTINATION.ID.eq(destinationId)
                        .and(DESTINATION.CURRENT));
        execute(query);
    }
}
