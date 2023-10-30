package dev.vality.daway.dao.invoicing.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.invoicing.iface.PaymentRecurrentInfoDao;
import dev.vality.daway.domain.tables.pojos.PaymentRecurrentInfo;
import dev.vality.daway.domain.tables.records.PaymentRecurrentInfoRecord;
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

import static dev.vality.daway.domain.tables.PaymentRecurrentInfo.PAYMENT_RECURRENT_INFO;

@Component
public class PaymentRecurrentInfoDaoImpl extends AbstractGenericDao implements PaymentRecurrentInfoDao {

    private final RowMapper<PaymentRecurrentInfo> rowMapper;

    public PaymentRecurrentInfoDaoImpl(DataSource dataSource) {
        super(dataSource);
        this.rowMapper = new RecordRowMapper<>(PAYMENT_RECURRENT_INFO, PaymentRecurrentInfo.class);
    }

    @Override
    public void saveBatch(List<PaymentRecurrentInfo> paymentRecurrentInfos) throws DaoException {
        List<Query> queries = paymentRecurrentInfos.stream()
                .map(statusInfo -> getDslContext().newRecord(PAYMENT_RECURRENT_INFO, statusInfo))
                .map(this::prepareInsertQuery)
                .collect(Collectors.toList());
        batchExecute(queries);
    }

    @Override
    public PaymentRecurrentInfo get(String invoiceId, String paymentId) throws DaoException {
        Query query = getDslContext().selectFrom(PAYMENT_RECURRENT_INFO)
                .where(PAYMENT_RECURRENT_INFO.INVOICE_ID.eq(invoiceId)
                        .and(PAYMENT_RECURRENT_INFO.PAYMENT_ID.eq(paymentId))
                        .and(PAYMENT_RECURRENT_INFO.CURRENT));
        return Optional.ofNullable(fetchOne(query, rowMapper)).orElseThrow(() ->
                new NotFoundException("PaymentPayerInfo not found, invoiceId=" + invoiceId + " paymentId=" + paymentId));
    }

    @Override
    public void switchCurrent(Set<InvoicingKey> invoicesSwitchIds) throws DaoException {
        invoicesSwitchIds.forEach(key -> {
            setOldRecurrentInfoNotCurrent(key);
            setLatestRecurrentInfoCurrent(key);
        });
    }

    private Query prepareInsertQuery(PaymentRecurrentInfoRecord record) {
        return getDslContext().insertInto(PAYMENT_RECURRENT_INFO)
                .set(record)
                .onConflict(
                        PAYMENT_RECURRENT_INFO.INVOICE_ID,
                        PAYMENT_RECURRENT_INFO.PAYMENT_ID,
                        PAYMENT_RECURRENT_INFO.SEQUENCE_ID,
                        PAYMENT_RECURRENT_INFO.CHANGE_ID
                )
                .doNothing();
    }

    private void setOldRecurrentInfoNotCurrent(InvoicingKey key) {
        execute(getDslContext().update(PAYMENT_RECURRENT_INFO)
                .set(PAYMENT_RECURRENT_INFO.CURRENT, false)
                .where(PAYMENT_RECURRENT_INFO.INVOICE_ID.eq(key.getInvoiceId())
                        .and(PAYMENT_RECURRENT_INFO.PAYMENT_ID.eq(key.getPaymentId()))
                        .and(PAYMENT_RECURRENT_INFO.CURRENT))
        );
    }

    private void setLatestRecurrentInfoCurrent(InvoicingKey key) {
        execute(getDslContext().update(PAYMENT_RECURRENT_INFO)
                .set(PAYMENT_RECURRENT_INFO.CURRENT, true)
                .where(PAYMENT_RECURRENT_INFO.ID.eq(
                        DSL.select(DSL.max(PAYMENT_RECURRENT_INFO.ID))
                                .from(PAYMENT_RECURRENT_INFO)
                                .where(PAYMENT_RECURRENT_INFO.INVOICE_ID.eq(key.getInvoiceId())
                                        .and(PAYMENT_RECURRENT_INFO.PAYMENT_ID.eq(key.getPaymentId())))
                ))
        );
    }
}
