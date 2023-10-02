package dev.vality.newway.dao.invoicing.iface;

import dev.vality.dao.GenericDao;
import dev.vality.newway.domain.tables.pojos.PaymentAdditionalInfo;
import dev.vality.newway.exception.DaoException;
import dev.vality.newway.model.InvoicingKey;

import java.util.List;
import java.util.Set;

public interface PaymentAdditionalInfoDao extends GenericDao {

    void saveBatch(List<PaymentAdditionalInfo> paymentAdditionalInfos) throws DaoException;

    PaymentAdditionalInfo get(String invoiceId, String paymentId) throws DaoException;

    PaymentAdditionalInfo getSafe(String invoiceId, String paymentId) throws DaoException;

    void switchCurrent(Set<InvoicingKey> invoicesSwitchIds) throws DaoException;

}