package dev.vality.daway.dao.invoicing.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.invoicing.iface.CashFlowLinkDao;
import dev.vality.daway.domain.tables.pojos.CashFlowLink;
import dev.vality.daway.exception.DaoException;
import dev.vality.daway.exception.NotFoundException;
import dev.vality.daway.model.InvoicePaymentEventIdHolder;
import dev.vality.daway.model.InvoicingKey;
import dev.vality.mapper.RecordRowMapper;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.vality.daway.domain.Tables.CASH_FLOW_LINK;

@Component
public class CashFlowLinkDaoImpl extends AbstractGenericDao implements CashFlowLinkDao {

    private final RowMapper<CashFlowLink> rowMapper;
    private final RowMapper<InvoicePaymentEventIdHolder> invoicePaymentEventIdRowMapper;

    private static final Field[] INVOICE_PAYMENT_EVENT_ID_HOLDER_FIELDS = new Field[]{
            CASH_FLOW_LINK.INVOICE_ID,
            CASH_FLOW_LINK.PAYMENT_ID,
            CASH_FLOW_LINK.CHANGE_ID,
            CASH_FLOW_LINK.SEQUENCE_ID
    };

    public CashFlowLinkDaoImpl(DataSource dataSource) {
        super(dataSource);
        rowMapper = new RecordRowMapper<>(CASH_FLOW_LINK, CashFlowLink.class);
        invoicePaymentEventIdRowMapper = new DataClassRowMapper<>(InvoicePaymentEventIdHolder.class);
    }

    @Override
    public void saveBatch(List<CashFlowLink> links) throws DaoException {
        batchExecute(links.stream()
                .map(status -> getDslContext().newRecord(CASH_FLOW_LINK, status))
                .map(invoiceStatusInfoRecord -> getDslContext().insertInto(CASH_FLOW_LINK)
                        .set(invoiceStatusInfoRecord))
                .collect(Collectors.toList())
        );
    }

    @Override
    public CashFlowLink get(String invoiceId, String paymentId) {
        Query query = getDslContext().selectFrom(CASH_FLOW_LINK)
                .where(CASH_FLOW_LINK.INVOICE_ID.eq(invoiceId)
                        .and(CASH_FLOW_LINK.PAYMENT_ID.eq(paymentId))
                        .and(CASH_FLOW_LINK.CURRENT));
        return Optional.ofNullable(fetchOne(query, rowMapper))
                .orElseThrow(() -> new NotFoundException(
                        String.format("CashFlowLink not found, invoiceId='%s', paymentId='%s'", invoiceId, paymentId)));
    }

    @Override
    public void switchCurrent(Set<InvoicingKey> keys) throws DaoException {
        keys.forEach(key -> {
            setOldCashFlowLinkNotCurrent(key);
            setLatestCashFlowLinkCurrent(key);
        });
    }

    @Override
    public Set<InvoicePaymentEventIdHolder> getExistingEvents(List<CashFlowLink> links) {
        Set<String> invoiceIds = new HashSet<>();
        Set<String> paymentIds = new HashSet<>();
        Set<Integer> changeIds = new HashSet<>();
        Set<Long> sequenceIds = new HashSet<>();
        Set<String> concatenatedIds = new HashSet<>();
        links.forEach(link -> {
            invoiceIds.add(link.getInvoiceId());
            paymentIds.add(link.getPaymentId());
            changeIds.add(link.getChangeId());
            sequenceIds.add(link.getSequenceId());
            concatenatedIds.add(link.getInvoiceId() + link.getPaymentId() + link.getChangeId() + link.getSequenceId());
        });

        // we have to use concatenated ids otherwise there is small probability of collision.
        // some non-processed events might fall under "invoice_id/payment_id/change_id/sequence_id in()" conditiona.
        // e.g. we will receive several cash_flow_change events within one batch
        //  and there will be overlap in change ids.
        // concat() is used as last step so there is minimal operation overhead.
        Query query = getDslContext()
                .select(INVOICE_PAYMENT_EVENT_ID_HOLDER_FIELDS)
                .from(CASH_FLOW_LINK)
                .where(CASH_FLOW_LINK.INVOICE_ID.in(invoiceIds))
                .and(CASH_FLOW_LINK.PAYMENT_ID.in(paymentIds))
                .and(CASH_FLOW_LINK.CHANGE_ID.in(changeIds))
                .and(CASH_FLOW_LINK.SEQUENCE_ID.in(sequenceIds))
                .and(DSL.concat(INVOICE_PAYMENT_EVENT_ID_HOLDER_FIELDS).in(concatenatedIds));

        return new HashSet<>(fetch(query, invoicePaymentEventIdRowMapper));
    }

    private void setOldCashFlowLinkNotCurrent(InvoicingKey key) {
        execute(getDslContext().update(CASH_FLOW_LINK)
                .set(CASH_FLOW_LINK.CURRENT, false)
                .where(CASH_FLOW_LINK.INVOICE_ID.eq(key.getInvoiceId())
                        .and(CASH_FLOW_LINK.PAYMENT_ID.eq(key.getPaymentId()))
                        .and(CASH_FLOW_LINK.CURRENT))
        );
    }

    private void setLatestCashFlowLinkCurrent(InvoicingKey key) {
        execute(getDslContext().update(CASH_FLOW_LINK)
                .set(CASH_FLOW_LINK.CURRENT, true)
                .where(CASH_FLOW_LINK.ID.eq(
                        DSL.select(DSL.max(CASH_FLOW_LINK.ID))
                                .from(CASH_FLOW_LINK)
                                .where(CASH_FLOW_LINK.INVOICE_ID.eq(key.getInvoiceId())
                                        .and(CASH_FLOW_LINK.PAYMENT_ID.eq(key.getPaymentId())))
                ))
        );
    }
}
