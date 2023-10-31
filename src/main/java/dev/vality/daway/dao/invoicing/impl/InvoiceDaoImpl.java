package dev.vality.daway.dao.invoicing.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.invoicing.iface.InvoiceDao;
import dev.vality.daway.domain.tables.pojos.Invoice;
import dev.vality.daway.domain.tables.records.InvoiceRecord;
import dev.vality.daway.exception.DaoException;
import dev.vality.daway.exception.NotFoundException;
import dev.vality.mapper.RecordRowMapper;
import org.jooq.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static dev.vality.daway.domain.tables.Invoice.INVOICE;

@Component
public class InvoiceDaoImpl extends AbstractGenericDao implements InvoiceDao {

    private final RowMapper<Invoice> invoiceRowMapper;

    @Autowired
    public InvoiceDaoImpl(DataSource dataSource) {
        super(dataSource);
        invoiceRowMapper = new RecordRowMapper<>(INVOICE, Invoice.class);
    }

    @Override
    public void saveBatch(List<Invoice> invoices) throws DaoException {
        List<Query> queries = invoices.stream()
                .map(invoice -> getDslContext().newRecord(INVOICE, invoice))
                .map(this::prepareInsertQuery)
                .collect(Collectors.toList());
        batchExecute(queries);
    }

    @NotNull
    @Override
    public Invoice get(String invoiceId) throws DaoException {
        Query query = getDslContext().selectFrom(INVOICE)
                .where(INVOICE.INVOICE_ID.eq(invoiceId));
        return Optional.ofNullable(fetchOne(query, invoiceRowMapper))
                .orElseThrow(
                        () -> new NotFoundException(String.format("Invoice not found, invoiceId='%s'", invoiceId)));
    }

    private Query prepareInsertQuery(InvoiceRecord invoiceRecord) {
        return getDslContext().insertInto(INVOICE)
                .set(invoiceRecord)
                .onConflict(
                        INVOICE.INVOICE_ID,
                        INVOICE.SEQUENCE_ID,
                        INVOICE.CHANGE_ID
                )
                .doNothing();
    }

}
