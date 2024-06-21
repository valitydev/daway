package dev.vality.daway.dao.withdrawal.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.withdrawal.iface.WithdrawalValidationDao;
import dev.vality.daway.domain.tables.pojos.WithdrawalValidation;
import dev.vality.daway.domain.tables.records.WithdrawalValidationRecord;
import dev.vality.daway.exception.DaoException;
import org.jooq.Query;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Optional;

import static dev.vality.daway.domain.tables.Withdrawal.WITHDRAWAL;
import static dev.vality.daway.domain.tables.WithdrawalValidation.WITHDRAWAL_VALIDATION;

@Component
public class WithdrawalValidationDaoImpl extends AbstractGenericDao implements WithdrawalValidationDao {

    public WithdrawalValidationDaoImpl(DataSource dataSource) {
        super(dataSource);
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
}
