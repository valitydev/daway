package dev.vality.daway.dao.invoicing.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.invoicing.iface.PaymentExchangeContextDao;
import dev.vality.daway.domain.tables.pojos.PaymentExchangeContext;
import dev.vality.daway.domain.tables.records.PaymentExchangeContextRecord;
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

import static dev.vality.daway.domain.Tables.PAYMENT_EXCHANGE_CONTEXT;

@Component
public class PaymentExchangeContextDaoImpl extends AbstractGenericDao implements PaymentExchangeContextDao {

    private final RowMapper<PaymentExchangeContext> paymentExchangeContextRowMapper;

    public PaymentExchangeContextDaoImpl(DataSource dataSource) {
        super(dataSource);
        paymentExchangeContextRowMapper = new RecordRowMapper<>(PAYMENT_EXCHANGE_CONTEXT, PaymentExchangeContext.class);
    }

    @Override
    public void saveBatch(List<PaymentExchangeContext> paymentExchangeContexts) throws DaoException {
        List<Query> queries = paymentExchangeContexts.stream()
                .map(paymentExchangeContext -> getDslContext().newRecord(
                        PAYMENT_EXCHANGE_CONTEXT, paymentExchangeContext))
                .map(this::prepareInsertQuery)
                .toList();
        batchExecute(queries);
    }

    private Query prepareInsertQuery(PaymentExchangeContextRecord record) {
        return getDslContext().insertInto(PAYMENT_EXCHANGE_CONTEXT)
                .set(record)
                .onConflict(
                        PAYMENT_EXCHANGE_CONTEXT.INVOICE_ID,
                        PAYMENT_EXCHANGE_CONTEXT.PAYMENT_ID,
                        PAYMENT_EXCHANGE_CONTEXT.SEQUENCE_ID,
                        PAYMENT_EXCHANGE_CONTEXT.CHANGE_ID
                )
                .doNothing();
    }

    @Override
    public void switchCurrent(Set<InvoicingKey> invoicingKeys) throws DaoException {
        invoicingKeys.forEach(key -> {
            setOldPaymentExchangeContextNotCurrent(key);
            setLatestPaymentExchangeContextCurrent(key);
        });
    }

    private void setOldPaymentExchangeContextNotCurrent(InvoicingKey key) {
        execute(getDslContext().update(PAYMENT_EXCHANGE_CONTEXT)
                .set(PAYMENT_EXCHANGE_CONTEXT.CURRENT, false)
                .where(PAYMENT_EXCHANGE_CONTEXT.INVOICE_ID.eq(key.getInvoiceId())
                        .and(PAYMENT_EXCHANGE_CONTEXT.PAYMENT_ID.eq(key.getPaymentId()))
                        .and(PAYMENT_EXCHANGE_CONTEXT.CURRENT))
        );
    }

    private void setLatestPaymentExchangeContextCurrent(InvoicingKey key) {
        execute(getDslContext().update(PAYMENT_EXCHANGE_CONTEXT)
                .set(PAYMENT_EXCHANGE_CONTEXT.CURRENT, true)
                .where(PAYMENT_EXCHANGE_CONTEXT.ID.eq(
                        DSL.select(DSL.max(PAYMENT_EXCHANGE_CONTEXT.ID))
                                .from(PAYMENT_EXCHANGE_CONTEXT)
                                .where(PAYMENT_EXCHANGE_CONTEXT.INVOICE_ID.eq(key.getInvoiceId())
                                        .and(PAYMENT_EXCHANGE_CONTEXT.PAYMENT_ID.eq(key.getPaymentId())))
                ))
        );
    }
}
