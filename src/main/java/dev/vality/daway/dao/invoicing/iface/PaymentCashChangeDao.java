package dev.vality.daway.dao.invoicing.iface;

import dev.vality.dao.GenericDao;
import dev.vality.daway.domain.tables.pojos.PaymentCashChange;
import dev.vality.daway.exception.DaoException;
import dev.vality.daway.model.InvoicingKey;

import java.util.List;
import java.util.Set;

public interface PaymentCashChangeDao extends GenericDao {

    void saveBatch(List<PaymentCashChange> paymentCashChanges) throws DaoException;

    void switchCurrent(Set<InvoicingKey> invoicingKeys) throws DaoException;

}
