package dev.vality.daway.dao.invoicing.iface;

import dev.vality.dao.GenericDao;
import dev.vality.daway.domain.tables.pojos.PaymentRecurrentInfo;
import dev.vality.daway.exception.DaoException;
import dev.vality.daway.model.InvoicingKey;

import java.util.List;
import java.util.Set;

public interface PaymentRecurrentInfoDao extends GenericDao {

    void saveBatch(List<PaymentRecurrentInfo> paymentRecurrentInfos) throws DaoException;

    PaymentRecurrentInfo get(String invoiceId, String paymentId) throws DaoException;

    void switchCurrent(Set<InvoicingKey> invoicesSwitchIds) throws DaoException;

}
