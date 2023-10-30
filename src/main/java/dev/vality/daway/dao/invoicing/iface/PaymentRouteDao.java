package dev.vality.daway.dao.invoicing.iface;

import dev.vality.dao.GenericDao;
import dev.vality.daway.domain.tables.pojos.PaymentRoute;
import dev.vality.daway.exception.DaoException;
import dev.vality.daway.model.InvoicingKey;

import java.util.List;
import java.util.Set;

public interface PaymentRouteDao extends GenericDao {

    void saveBatch(List<PaymentRoute> paymentRoutes) throws DaoException;

    PaymentRoute get(String invoiceId, String paymentId) throws DaoException;

    PaymentRoute safeGet(String invoiceId, String paymentId) throws DaoException;

    void switchCurrent(Set<InvoicingKey> invoicingKeys) throws DaoException;

}
