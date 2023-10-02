package dev.vality.newway.dao.invoicing.iface;

import dev.vality.dao.GenericDao;
import dev.vality.newway.domain.tables.pojos.PaymentRoute;
import dev.vality.newway.exception.DaoException;
import dev.vality.newway.model.InvoicingKey;

import java.util.List;
import java.util.Set;

public interface PaymentRouteDao extends GenericDao {

    void saveBatch(List<PaymentRoute> paymentRoutes) throws DaoException;

    PaymentRoute get(String invoiceId, String paymentId) throws DaoException;

    PaymentRoute getSafe(String invoiceId, String paymentId) throws DaoException;

    void switchCurrent(Set<InvoicingKey> invoicingKeys) throws DaoException;

}
