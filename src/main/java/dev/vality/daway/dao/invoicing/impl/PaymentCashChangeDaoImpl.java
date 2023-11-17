package dev.vality.daway.dao.invoicing.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.invoicing.iface.PaymentCashChangeDao;
import dev.vality.daway.domain.tables.pojos.PaymentCashChange;
import dev.vality.daway.domain.tables.records.PaymentCashChangeRecord;
import dev.vality.daway.domain.tables.records.PaymentFeeRecord;
import dev.vality.daway.exception.DaoException;
import dev.vality.daway.model.InvoicingKey;
import dev.vality.mapper.RecordRowMapper;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.vality.daway.domain.Tables.PAYMENT_CASH_CHANGE;
import static dev.vality.daway.domain.tables.PaymentFee.PAYMENT_FEE;

@Component
public class PaymentCashChangeDaoImpl extends AbstractGenericDao implements PaymentCashChangeDao {

    private final RowMapper<PaymentCashChange> cashChangeRowMapper;

    public PaymentCashChangeDaoImpl(DataSource dataSource) {
        super(dataSource);
        cashChangeRowMapper = new RecordRowMapper<>(PAYMENT_CASH_CHANGE, PaymentCashChange.class);
    }

    @Override
    public void saveBatch(List<PaymentCashChange> paymentCashChanges) throws DaoException {
        List<Query> queries = paymentCashChanges.stream()
                .map(cashChange -> getDslContext().newRecord(PAYMENT_CASH_CHANGE, cashChange))
                .map(this::prepareInsertQuery)
                .collect(Collectors.toList());
        batchExecute(queries);
    }

    private Query prepareInsertQuery(PaymentCashChangeRecord record) {
        return getDslContext().insertInto(PAYMENT_CASH_CHANGE)
                .set(record)
                .onConflict(
                        PAYMENT_CASH_CHANGE.INVOICE_ID,
                        PAYMENT_CASH_CHANGE.PAYMENT_ID,
                        PAYMENT_CASH_CHANGE.SEQUENCE_ID,
                        PAYMENT_CASH_CHANGE.CHANGE_ID
                )
                .doNothing();
    }

    @Override
    public void switchCurrent(Set<InvoicingKey> invoicingKeys) throws DaoException {
        invoicingKeys.forEach(key -> {
            setOldPaymentFeeNotCurrent(key);
            setLatestPaymentFeeCurrent(key);
        });
    }

    private void setOldPaymentFeeNotCurrent(InvoicingKey key) {
        execute(getDslContext().update(PAYMENT_CASH_CHANGE)
                .set(PAYMENT_CASH_CHANGE.CURRENT, false)
                .where(PAYMENT_CASH_CHANGE.INVOICE_ID.eq(key.getInvoiceId())
                        .and(PAYMENT_CASH_CHANGE.PAYMENT_ID.eq(key.getPaymentId()))
                        .and(PAYMENT_CASH_CHANGE.CURRENT))
        );
    }

    private void setLatestPaymentFeeCurrent(InvoicingKey key) {
        execute(getDslContext().update(PAYMENT_CASH_CHANGE)
                .set(PAYMENT_CASH_CHANGE.CURRENT, true)
                .where(PAYMENT_CASH_CHANGE.ID.eq(
                        DSL.select(DSL.max(PAYMENT_CASH_CHANGE.ID))
                                .from(PAYMENT_CASH_CHANGE)
                                .where(PAYMENT_CASH_CHANGE.INVOICE_ID.eq(key.getInvoiceId())
                                        .and(PAYMENT_CASH_CHANGE.PAYMENT_ID.eq(key.getPaymentId())))
                ))
        );
    }
}
