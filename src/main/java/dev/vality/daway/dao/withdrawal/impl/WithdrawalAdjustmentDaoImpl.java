package dev.vality.daway.dao.withdrawal.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.withdrawal.iface.WithdrawalAdjustmentDao;
import dev.vality.daway.domain.tables.pojos.WithdrawalAdjustment;
import dev.vality.daway.domain.tables.records.WithdrawalAdjustmentRecord;
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

import static dev.vality.daway.domain.tables.WithdrawalAdjustment.WITHDRAWAL_ADJUSTMENT;

@Component
public class WithdrawalAdjustmentDaoImpl extends AbstractGenericDao implements WithdrawalAdjustmentDao {

    private final RowMapper<WithdrawalAdjustment> withdrawalAdjustmentRowMapper;

    @Autowired
    public WithdrawalAdjustmentDaoImpl(DataSource dataSource) {
        super(dataSource);
        withdrawalAdjustmentRowMapper = new RecordRowMapper<>(WITHDRAWAL_ADJUSTMENT, WithdrawalAdjustment.class);
    }

    @Override
    public Optional<Long> save(WithdrawalAdjustment withdrawalAdjustment) throws DaoException {
        WithdrawalAdjustmentRecord adjustmentRecord = getDslContext().newRecord(WITHDRAWAL_ADJUSTMENT, withdrawalAdjustment);
        Query query = getDslContext()
                .insertInto(WITHDRAWAL_ADJUSTMENT)
                .set(adjustmentRecord)
                .onConflict(WITHDRAWAL_ADJUSTMENT.ADJUSTMENT_ID, WITHDRAWAL_ADJUSTMENT.WITHDRAWAL_ID, WITHDRAWAL_ADJUSTMENT.SEQUENCE_ID)
                .doNothing()
                .returning(WITHDRAWAL_ADJUSTMENT.ID);

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        execute(query, keyHolder);
        return Optional.ofNullable(keyHolder.getKey()).map(Number::longValue);
    }

    @Override
    public WithdrawalAdjustment getByIds(String withdrawalId, String withdrawalAdjustmentId) throws DaoException {
        Query query = getDslContext().selectFrom(WITHDRAWAL_ADJUSTMENT)
                .where(WITHDRAWAL_ADJUSTMENT.ADJUSTMENT_ID.eq(withdrawalAdjustmentId)
                        .and(WITHDRAWAL_ADJUSTMENT.WITHDRAWAL_ID.eq(withdrawalId))
                        .and(WITHDRAWAL_ADJUSTMENT.CURRENT));
        return Optional.ofNullable(fetchOne(query, withdrawalAdjustmentRowMapper))
                .orElseThrow(() -> new NotFoundException(
                        String.format("WithdrawalAdjustment not found, withdrawalAdjustmentId='%s', withdrawalId='%s'",
                                withdrawalAdjustmentId, withdrawalId)));
    }

    @Override
    public void updateNotCurrent(Long id) throws DaoException {
        Query query = getDslContext().update(WITHDRAWAL_ADJUSTMENT).set(WITHDRAWAL_ADJUSTMENT.CURRENT, false)
                .where(WITHDRAWAL_ADJUSTMENT.ID.eq(id)
                        .and(WITHDRAWAL_ADJUSTMENT.CURRENT));
        execute(query);
    }
}
