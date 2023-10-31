package dev.vality.daway.dao.invoicing.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.invoicing.iface.InvoiceCartDao;
import dev.vality.daway.domain.tables.pojos.InvoiceCart;
import dev.vality.daway.exception.DaoException;
import dev.vality.mapper.RecordRowMapper;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.vality.daway.domain.tables.InvoiceCart.INVOICE_CART;

@Component
public class InvoiceCartDaoImpl extends AbstractGenericDao implements InvoiceCartDao {

    private final RowMapper<InvoiceCart> invoiceCartRowMapper;

    @Autowired
    public InvoiceCartDaoImpl(DataSource dataSource) {
        super(dataSource);
        invoiceCartRowMapper = new RecordRowMapper<>(INVOICE_CART, InvoiceCart.class);
    }

    @Override
    public void save(List<InvoiceCart> carts) throws DaoException {
        List<Query> queries = carts.stream()
                .map(cart -> getDslContext().newRecord(INVOICE_CART, cart))
                .map(cartRecord -> getDslContext().insertInto(INVOICE_CART).set(cartRecord))
                .collect(Collectors.toList());
        batchExecute(queries);
    }

    @Override
    public List<InvoiceCart> getByInvoiceId(String invoiceId) throws DaoException {
        Query query = getDslContext().selectFrom(INVOICE_CART)
                .where(INVOICE_CART.INVOICE_ID.eq(invoiceId));
        return fetch(query, invoiceCartRowMapper);
    }

    /**
     * Invoice cart can be written only once when Invoice is created.
     * Invoice cart cannot be changed, only way to change invoice cart is to cancel invoice and create new one.
     *
     * @param invoiceIds set of invoice ids to check for existence.
     * @return List of invoice ids which haven't been saved already.
     * @throws DaoException
     */
    @Override
    public Set<String> getExistingInvoiceIds(Set<String> invoiceIds) throws DaoException {
        Query query = getDslContext()
                .select(INVOICE_CART.INVOICE_ID)
                .from(INVOICE_CART)
                .where(INVOICE_CART.INVOICE_ID.in(invoiceIds))
                .groupBy(INVOICE_CART.INVOICE_ID)
                .having(DSL.count(INVOICE_CART.ID).greaterThan(0));

        return new HashSet<>(fetch(query, new SingleColumnRowMapper<>(String.class)));
    }
}
