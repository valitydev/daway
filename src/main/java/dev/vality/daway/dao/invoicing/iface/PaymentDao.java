package dev.vality.daway.dao.invoicing.iface;

import dev.vality.dao.GenericDao;
import dev.vality.daway.domain.tables.pojos.Payment;
import dev.vality.daway.exception.DaoException;

import java.util.List;

public interface PaymentDao extends GenericDao {

    void saveBatch(List<Payment> payments) throws DaoException;

    Payment get(String invoiceId, String paymentId) throws DaoException;

}