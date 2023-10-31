package dev.vality.daway.dao.payout.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.payout.iface.PayoutDao;
import dev.vality.daway.domain.tables.pojos.Payout;
import dev.vality.daway.domain.tables.records.PayoutRecord;
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

import static dev.vality.daway.domain.Tables.PAYOUT;

@Component
public class PayoutDaoImpl extends AbstractGenericDao implements PayoutDao {

    private final RowMapper<Payout> payoutRowMapper;

    @Autowired
    public PayoutDaoImpl(DataSource dataSource) {
        super(dataSource);
        payoutRowMapper = new RecordRowMapper<>(PAYOUT, Payout.class);
    }

    @Override
    public Optional<Long> save(Payout payout) throws DaoException {
        PayoutRecord payoutRecord = getDslContext().newRecord(PAYOUT, payout);
        Query query = getDslContext()
                .insertInto(PAYOUT)
                .set(payoutRecord)
                .onConflict(PAYOUT.PAYOUT_ID, PAYOUT.SEQUENCE_ID)
                .doNothing()
                .returning(PAYOUT.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        execute(query, keyHolder);
        return Optional.ofNullable(keyHolder.getKey()).map(Number::longValue);
    }

    @Override
    public Payout get(String payoutId) throws DaoException {
        Query query = getDslContext().selectFrom(PAYOUT)
                .where(PAYOUT.PAYOUT_ID.eq(payoutId).and(PAYOUT.CURRENT));
        return Optional.ofNullable(fetchOne(query, payoutRowMapper))
                .orElseThrow(() -> new NotFoundException(String.format("Payout not found, payoutId='%s'", payoutId)));
    }

    @Override
    public void updateNotCurrent(Long payoutId) throws DaoException {
        Query query = getDslContext().update(PAYOUT).set(PAYOUT.CURRENT, false)
                .where(PAYOUT.ID.eq(payoutId)
                        .and(PAYOUT.CURRENT));
        executeOne(query);
    }
}
