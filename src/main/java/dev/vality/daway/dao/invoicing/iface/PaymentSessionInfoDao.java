package dev.vality.daway.dao.invoicing.iface;

import dev.vality.dao.GenericDao;
import dev.vality.daway.domain.tables.pojos.PaymentSessionInfo;
import dev.vality.daway.exception.DaoException;

import java.util.List;

public interface PaymentSessionInfoDao extends GenericDao {

    void saveBatch(List<PaymentSessionInfo> paymentStatusInfos) throws DaoException;

    PaymentSessionInfo get(String invoiceId, String paymentId) throws DaoException;
}