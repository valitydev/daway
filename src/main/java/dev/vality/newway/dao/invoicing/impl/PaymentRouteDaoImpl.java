package dev.vality.newway.dao.invoicing.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.mapper.RecordRowMapper;
import dev.vality.newway.dao.invoicing.iface.PaymentRouteDao;
import dev.vality.newway.domain.tables.pojos.PaymentRoute;
import dev.vality.newway.domain.tables.records.PaymentRouteRecord;
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

import static dev.vality.newway.domain.tables.PaymentRoute.PAYMENT_ROUTE;

@Component
public class PaymentRouteDaoImpl extends AbstractGenericDao implements PaymentRouteDao {

    private final RowMapper<PaymentRoute> rowMapper;

    public PaymentRouteDaoImpl(DataSource dataSource) {
        super(dataSource);
        this.rowMapper = new RecordRowMapper<>(PAYMENT_ROUTE, PaymentRoute.class);
    }

    @Override
    public void saveBatch(List<PaymentRoute> paymentRoutes) throws DaoException {
        List<Query> queries = paymentRoutes.stream()
                .map(statusInfo -> getDslContext().newRecord(PAYMENT_ROUTE, statusInfo))
                .map(this::prepareInsertQuery)
                .collect(Collectors.toList());
        batchExecute(queries);
    }

    @Override
    public PaymentRoute get(String invoiceId, String paymentId) throws DaoException {
        Query query = getDslContext().selectFrom(PAYMENT_ROUTE)
                .where(PAYMENT_ROUTE.INVOICE_ID.eq(invoiceId)
                        .and(PAYMENT_ROUTE.PAYMENT_ID.eq(paymentId))
                        .and(PAYMENT_ROUTE.CURRENT)
                );
        return Optional.ofNullable(fetchOne(query, rowMapper)).orElseThrow(() ->
                new NotFoundException("PaymentRoute not found, invoiceId=" + invoiceId + " paymentId=" + paymentId));
    }

    @Override
    public PaymentRoute getSafe(String invoiceId, String paymentId) throws DaoException {
        Query query = getDslContext().selectFrom(PAYMENT_ROUTE)
                .where(PAYMENT_ROUTE.INVOICE_ID.eq(invoiceId)
                        .and(PAYMENT_ROUTE.PAYMENT_ID.eq(paymentId))
                        .and(PAYMENT_ROUTE.CURRENT)
                );
        return fetchOne(query, rowMapper);
    }

    @Override
    public void switchCurrent(Set<InvoicingKey> invoicingKeys) throws DaoException {
        invoicingKeys.forEach(key -> {
            setOldRouteNotCurrent(key);
            setLatestRouteCurrent(key);
        });
    }

    private void setOldRouteNotCurrent(InvoicingKey key) {
        execute(getDslContext().update(PAYMENT_ROUTE)
                .set(PAYMENT_ROUTE.CURRENT, false)
                .where(PAYMENT_ROUTE.INVOICE_ID.eq(key.getInvoiceId())
                        .and(PAYMENT_ROUTE.PAYMENT_ID.eq(key.getPaymentId()))
                        .and(PAYMENT_ROUTE.CURRENT))
        );
    }

    private void setLatestRouteCurrent(InvoicingKey key) {
        execute(getDslContext().update(PAYMENT_ROUTE)
                .set(PAYMENT_ROUTE.CURRENT, true)
                .where(PAYMENT_ROUTE.ID.eq(
                        DSL.select(DSL.max(PAYMENT_ROUTE.ID))
                                .from(PAYMENT_ROUTE)
                                .where(PAYMENT_ROUTE.INVOICE_ID.eq(key.getInvoiceId())
                                        .and(PAYMENT_ROUTE.PAYMENT_ID.eq(key.getPaymentId())))
                ))
        );
    }

    private Query prepareInsertQuery(PaymentRouteRecord record) {
        return getDslContext().insertInto(PAYMENT_ROUTE)
                .set(record)
                .onConflict(
                        PAYMENT_ROUTE.INVOICE_ID,
                        PAYMENT_ROUTE.PAYMENT_ID,
                        PAYMENT_ROUTE.SEQUENCE_ID,
                        PAYMENT_ROUTE.CHANGE_ID
                )
                .doNothing();
    }
}
