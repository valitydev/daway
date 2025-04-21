package dev.vality.daway.dao.invoicing.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.invoicing.iface.PaymentFeeDao;
import dev.vality.daway.domain.tables.pojos.PaymentFee;
import dev.vality.daway.domain.tables.records.PaymentFeeRecord;
import dev.vality.daway.exception.DaoException;
import dev.vality.daway.exception.NotFoundException;
import dev.vality.daway.model.InvoicingKey;
import dev.vality.mapper.RecordRowMapper;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.vality.daway.domain.tables.PaymentFee.PAYMENT_FEE;

@Component
public class PaymentFeeDaoImpl extends AbstractGenericDao implements PaymentFeeDao {

    private final RowMapper<PaymentFee> rowMapper;

    public PaymentFeeDaoImpl(DataSource dataSource) {
        super(dataSource);
        this.rowMapper = new RecordRowMapper<>(PAYMENT_FEE, PaymentFee.class);
    }

    @Override
    public void saveBatch(List<PaymentFee> paymentFees) throws DaoException {
        List<Query> queries = paymentFees.stream()
                .map(statusInfo -> getDslContext().newRecord(PAYMENT_FEE, statusInfo))
                .map(this::prepareInsertQuery)
                .collect(Collectors.toList());
        batchExecute(queries);
    }

    @Override
    public PaymentFee get(String invoiceId, String paymentId) throws DaoException {
        Query query = getDslContext().selectFrom(PAYMENT_FEE)
                .where(PAYMENT_FEE.INVOICE_ID.eq(invoiceId)
                        .and(PAYMENT_FEE.PAYMENT_ID.eq(paymentId))
                        .and(PAYMENT_FEE.CURRENT)
                );
        return Optional.ofNullable(fetchOne(query, rowMapper)).orElseThrow(() ->
                new NotFoundException("PaymentFee not found, invoiceId=" + invoiceId + " paymentId=" + paymentId));
    }

    @Override
    public void switchCurrent(Set<InvoicingKey> invoicingKeys) throws DaoException {
        invoicingKeys.forEach(key -> {
            setOldPaymentFeeNotCurrent(key);
            setLatestPaymentFeeCurrent(key);
        });
    }

    private Query prepareInsertQuery(PaymentFeeRecord record) {
        return getDslContext().insertInto(PAYMENT_FEE)
                .set(record)
                .onConflict(
                        PAYMENT_FEE.INVOICE_ID,
                        PAYMENT_FEE.PAYMENT_ID,
                        PAYMENT_FEE.SEQUENCE_ID,
                        PAYMENT_FEE.CHANGE_ID,
                        PAYMENT_FEE.EVENT_CREATED_AT
                )
                .doNothing();
    }

    private void setOldPaymentFeeNotCurrent(InvoicingKey key) {
        execute(getDslContext().update(PAYMENT_FEE)
                .set(PAYMENT_FEE.CURRENT, false)
                .where(PAYMENT_FEE.INVOICE_ID.eq(key.getInvoiceId())
                        .and(PAYMENT_FEE.PAYMENT_ID.eq(key.getPaymentId()))
                        .and(PAYMENT_FEE.CURRENT))
        );
    }

    private void setLatestPaymentFeeCurrent(InvoicingKey key) {
        execute(getDslContext().update(PAYMENT_FEE)
                .set(PAYMENT_FEE.CURRENT, true)
                .where(PAYMENT_FEE.ID.eq(
                        DSL.select(DSL.max(PAYMENT_FEE.ID))
                                .from(PAYMENT_FEE)
                                .where(PAYMENT_FEE.INVOICE_ID.eq(key.getInvoiceId())
                                        .and(PAYMENT_FEE.PAYMENT_ID.eq(key.getPaymentId())))
                ))
        );
    }
}
