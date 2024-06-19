package dev.vality.daway.dao.withdrawal.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.withdrawal.iface.WithdrawalValidationDao;
import dev.vality.daway.domain.tables.pojos.WithdrawalValidation;
import dev.vality.daway.domain.tables.records.WithdrawalValidationRecord;
import dev.vality.daway.exception.DaoException;
import dev.vality.daway.exception.NotFoundException;
import dev.vality.mapper.RecordRowMapper;
import org.jooq.Query;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Optional;

import static dev.vality.daway.domain.tables.Withdrawal.WITHDRAWAL;
import static dev.vality.daway.domain.tables.WithdrawalValidation.WITHDRAWAL_VALIDATION;

@Component
public class WithdrawalValidationDaoImpl extends AbstractGenericDao implements WithdrawalValidationDao {

    private final RowMapper<WithdrawalValidation> withdrawalValidationRowMapper;

    public WithdrawalValidationDaoImpl(DataSource dataSource) {
        super(dataSource);
        this.withdrawalValidationRowMapper = new RecordRowMapper<>(WITHDRAWAL_VALIDATION, WithdrawalValidation.class);
    }

    @Override
    public Optional<Long> save(WithdrawalValidation withdrawalValidation) throws DaoException {
        WithdrawalValidationRecord withdrawalValidationRecord = getDslContext().newRecord(WITHDRAWAL_VALIDATION, withdrawalValidation);
        Query query = getDslContext()
                .insertInto(WITHDRAWAL_VALIDATION)
                .set(withdrawalValidationRecord)
                .onConflict(WITHDRAWAL_VALIDATION.VALIDATION_ID, WITHDRAWAL.SEQUENCE_ID)
                .doNothing()
                .returning(WITHDRAWAL_VALIDATION.ID);

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        execute(query, keyHolder);
        return Optional.ofNullable(keyHolder.getKey()).map(Number::longValue);
    }

    @Override
    public WithdrawalValidation get(String withdrawalId, String validationId) throws DaoException {
        Query query = getDslContext().selectFrom(WITHDRAWAL_VALIDATION)
                .where(WITHDRAWAL_VALIDATION.VALIDATION_ID.eq(validationId)
                        .and(WITHDRAWAL_VALIDATION.WITHDRAWAL_ID.eq(withdrawalId))
                        .and(WITHDRAWAL_VALIDATION.CURRENT));
        return Optional.ofNullable(fetchOne(query, withdrawalValidationRowMapper))
                .orElseThrow(() -> new NotFoundException(
                        String.format("WithdrawalValidation not found, validationId='%s', withdrawalId='%s'",
                                validationId, withdrawalId)));
    }

    @Override
    public void updateNotCurrent(Long withdrawalValidationId) throws DaoException {
        Query query = getDslContext().update(WITHDRAWAL_VALIDATION).set(WITHDRAWAL_VALIDATION.CURRENT, false)
                .where(WITHDRAWAL_VALIDATION.ID.eq(withdrawalValidationId)
                        .and(WITHDRAWAL_VALIDATION.CURRENT));
        execute(query);
    }
}
