package dev.vality.daway.dao.invoicing.iface;

import dev.vality.dao.GenericDao;
import dev.vality.daway.domain.tables.pojos.PaymentPayerInfo;
import dev.vality.daway.exception.DaoException;

import java.util.List;

public interface PaymentPayerInfoDao extends GenericDao {

    void saveBatch(List<PaymentPayerInfo> payerInfos) throws DaoException;

    PaymentPayerInfo get(String invoiceId, String paymentId) throws DaoException;

}
