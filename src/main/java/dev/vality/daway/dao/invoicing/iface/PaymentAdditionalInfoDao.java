package dev.vality.daway.dao.invoicing.iface;

import dev.vality.dao.GenericDao;
import dev.vality.daway.domain.tables.pojos.PaymentAdditionalInfo;
import dev.vality.daway.exception.DaoException;
import dev.vality.daway.model.InvoicingKey;

import java.util.List;
import java.util.Set;

public interface PaymentAdditionalInfoDao extends GenericDao {

    void saveBatch(List<PaymentAdditionalInfo> paymentAdditionalInfos) throws DaoException;

    PaymentAdditionalInfo get(String invoiceId, String paymentId) throws DaoException;

    PaymentAdditionalInfo safeGet(String invoiceId, String paymentId) throws DaoException;

    void switchCurrent(Set<InvoicingKey> invoicesSwitchIds) throws DaoException;

}