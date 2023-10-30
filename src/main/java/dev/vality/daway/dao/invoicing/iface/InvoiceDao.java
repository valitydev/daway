package dev.vality.daway.dao.invoicing.iface;

import dev.vality.dao.GenericDao;
import dev.vality.daway.domain.tables.pojos.Invoice;
import dev.vality.daway.exception.DaoException;

import java.util.List;

public interface InvoiceDao extends GenericDao {

    void saveBatch(List<Invoice> invoices) throws DaoException;

    Invoice get(String invoiceId) throws DaoException;

}