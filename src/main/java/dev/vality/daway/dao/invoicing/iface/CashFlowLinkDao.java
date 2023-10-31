package dev.vality.daway.dao.invoicing.iface;

import dev.vality.dao.GenericDao;
import dev.vality.daway.domain.tables.pojos.CashFlowLink;
import dev.vality.daway.exception.DaoException;
import dev.vality.daway.model.InvoicePaymentEventIdHolder;
import dev.vality.daway.model.InvoicingKey;

import java.util.List;
import java.util.Set;

public interface CashFlowLinkDao extends GenericDao {

    void saveBatch(List<CashFlowLink> links) throws DaoException;

    CashFlowLink get(String invoiceId, String paymentId);

    void switchCurrent(Set<InvoicingKey> keys) throws DaoException;

    Set<InvoicePaymentEventIdHolder> getExistingEvents(List<CashFlowLink> links);

}
