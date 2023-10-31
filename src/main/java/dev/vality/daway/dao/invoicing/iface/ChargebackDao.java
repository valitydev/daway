package dev.vality.daway.dao.invoicing.iface;

import dev.vality.dao.GenericDao;
import dev.vality.daway.domain.tables.pojos.Chargeback;
import dev.vality.daway.exception.DaoException;

import java.util.Optional;

public interface ChargebackDao extends GenericDao {

    Optional<Long> save(Chargeback chargeback) throws DaoException;

    Chargeback get(String invoiceId, String paymentId, String chargebackId) throws DaoException;

    void updateNotCurrent(Long id) throws DaoException;

}
