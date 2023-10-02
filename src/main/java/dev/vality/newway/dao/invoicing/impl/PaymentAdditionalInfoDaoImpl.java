package dev.vality.newway.dao.invoicing.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.mapper.RecordRowMapper;
import dev.vality.newway.dao.invoicing.iface.PaymentAdditionalInfoDao;
import dev.vality.newway.domain.tables.pojos.PaymentAdditionalInfo;
import dev.vality.newway.domain.tables.records.PaymentAdditionalInfoRecord;
import dev.vality.newway.exception.DaoException;
import dev.vality.newway.exception.NotFoundException;
import dev.vality.newway.model.InvoicingKey;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.vality.newway.domain.tables.PaymentAdditionalInfo.PAYMENT_ADDITIONAL_INFO;

@Component
public class PaymentAdditionalInfoDaoImpl extends AbstractGenericDao implements PaymentAdditionalInfoDao {
    
    private final RowMapper<PaymentAdditionalInfo> rowMapper;
    
    public PaymentAdditionalInfoDaoImpl(DataSource dataSource) {
        super(dataSource);
        this.rowMapper = new RecordRowMapper<>(PAYMENT_ADDITIONAL_INFO, PaymentAdditionalInfo.class);

    }

    @Override
    public void saveBatch(List<PaymentAdditionalInfo> paymentAdditionalInfos) throws DaoException {
        List<Query> queries = paymentAdditionalInfos.stream()
                .map(statusInfo -> getDslContext().newRecord(PAYMENT_ADDITIONAL_INFO, statusInfo))
                .map(this::prepareInsertQuery
                )
                .collect(Collectors.toList());
        batchExecute(queries);
    }

    @Override
    public PaymentAdditionalInfo get(String invoiceId, String paymentId) throws DaoException {
        Query query = getDslContext().selectFrom(PAYMENT_ADDITIONAL_INFO)
                .where(PAYMENT_ADDITIONAL_INFO.INVOICE_ID.eq(invoiceId)
                        .and(PAYMENT_ADDITIONAL_INFO.PAYMENT_ID.eq(paymentId))
                        .and(PAYMENT_ADDITIONAL_INFO.CURRENT));
        return Optional.ofNullable(fetchOne(query, rowMapper)).orElseThrow(() ->
                new NotFoundException("PaymentAdditionalInfo not found, invoiceId=" + invoiceId + " paymentId=" + paymentId));
    }

    @Override
    public PaymentAdditionalInfo getSafe(String invoiceId, String paymentId) throws DaoException {
        Query query = getDslContext().selectFrom(PAYMENT_ADDITIONAL_INFO)
                .where(PAYMENT_ADDITIONAL_INFO.INVOICE_ID.eq(invoiceId)
                        .and(PAYMENT_ADDITIONAL_INFO.PAYMENT_ID.eq(paymentId))
                        .and(PAYMENT_ADDITIONAL_INFO.CURRENT));
        return fetchOne(query, rowMapper);
    }

    @Override
    public void switchCurrent(Set<InvoicingKey> invoicesSwitchIds) throws DaoException {
        invoicesSwitchIds.forEach(key -> {
            setOldAdditionalInfoNotCurrent(key);
            setLatestAdditionalInfoCurrent(key);
        });
    }

    private Query prepareInsertQuery(PaymentAdditionalInfoRecord record) {
        return getDslContext().insertInto(PAYMENT_ADDITIONAL_INFO)
                .set(record)
                .onConflict(
                        PAYMENT_ADDITIONAL_INFO.INVOICE_ID,
                        PAYMENT_ADDITIONAL_INFO.PAYMENT_ID,
                        PAYMENT_ADDITIONAL_INFO.SEQUENCE_ID,
                        PAYMENT_ADDITIONAL_INFO.CHANGE_ID
                )
                .doNothing();
    }

    private void setOldAdditionalInfoNotCurrent(InvoicingKey key) {
        execute(getDslContext().update(PAYMENT_ADDITIONAL_INFO)
                .set(PAYMENT_ADDITIONAL_INFO.CURRENT, false)
                .where(PAYMENT_ADDITIONAL_INFO.INVOICE_ID.eq(key.getInvoiceId())
                        .and(PAYMENT_ADDITIONAL_INFO.PAYMENT_ID.eq(key.getPaymentId()))
                        .and(PAYMENT_ADDITIONAL_INFO.CURRENT))
        );
    }

    private void setLatestAdditionalInfoCurrent(InvoicingKey key) {
        execute(getDslContext().update(PAYMENT_ADDITIONAL_INFO)
                .set(PAYMENT_ADDITIONAL_INFO.CURRENT, true)
                .where(PAYMENT_ADDITIONAL_INFO.ID.eq(
                        DSL.select(DSL.max(PAYMENT_ADDITIONAL_INFO.ID))
                                .from(PAYMENT_ADDITIONAL_INFO)
                                .where(PAYMENT_ADDITIONAL_INFO.INVOICE_ID.eq(key.getInvoiceId())
                                        .and(PAYMENT_ADDITIONAL_INFO.PAYMENT_ID.eq(key.getPaymentId())))
                ))
        );
    }
}
