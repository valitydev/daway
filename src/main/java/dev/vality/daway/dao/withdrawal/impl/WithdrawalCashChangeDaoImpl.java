package dev.vality.daway.dao.withdrawal.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.withdrawal.iface.WithdrawalCashChangeDao;
import dev.vality.daway.domain.tables.pojos.WithdrawalCashChange;
import dev.vality.daway.domain.tables.records.WithdrawalCashChangeRecord;
import dev.vality.daway.exception.DaoException;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static dev.vality.daway.domain.Tables.WITHDRAWAL_CASH_CHANGE;

@Component
public class WithdrawalCashChangeDaoImpl extends AbstractGenericDao implements WithdrawalCashChangeDao {

    public WithdrawalCashChangeDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void save(WithdrawalCashChange withdrawalCashChange) throws DaoException {
        WithdrawalCashChangeRecord record = getDslContext().newRecord(WITHDRAWAL_CASH_CHANGE, withdrawalCashChange);
        execute(prepareInsertQuery(record));
    }

    private Query prepareInsertQuery(WithdrawalCashChangeRecord record) {
        return getDslContext().insertInto(WITHDRAWAL_CASH_CHANGE)
                .set(record)
                .onConflict(
                        WITHDRAWAL_CASH_CHANGE.WITHDRAWAL_ID,
                        WITHDRAWAL_CASH_CHANGE.SEQUENCE_ID
                )
                .doNothing();
    }

    @Override
    public void switchCurrent(String withdrawalId) throws DaoException {
        setOldWithdrawalCashChangeNotCurrent(withdrawalId);
        setLatestWithdrawalCashChangeCurrent(withdrawalId);
    }

    private void setOldWithdrawalCashChangeNotCurrent(String withdrawalId) {
        execute(getDslContext().update(WITHDRAWAL_CASH_CHANGE)
                .set(WITHDRAWAL_CASH_CHANGE.CURRENT, false)
                .where(WITHDRAWAL_CASH_CHANGE.WITHDRAWAL_ID.eq(withdrawalId)
                        .and(WITHDRAWAL_CASH_CHANGE.CURRENT))
        );
    }

    private void setLatestWithdrawalCashChangeCurrent(String withdrawalId) {
        execute(getDslContext().update(WITHDRAWAL_CASH_CHANGE)
                .set(WITHDRAWAL_CASH_CHANGE.CURRENT, true)
                .where(WITHDRAWAL_CASH_CHANGE.ID.eq(
                        DSL.select(DSL.max(WITHDRAWAL_CASH_CHANGE.ID))
                                .from(WITHDRAWAL_CASH_CHANGE)
                                .where(WITHDRAWAL_CASH_CHANGE.WITHDRAWAL_ID.eq(withdrawalId))
                ))
        );
    }
}
