package dev.vality.daway.dao.invoicing.iface;

import dev.vality.dao.GenericDao;
import dev.vality.daway.domain.tables.pojos.Adjustment;
import dev.vality.daway.exception.DaoException;

import java.util.Optional;

public interface AdjustmentDao extends GenericDao {

    Optional<Long> save(Adjustment adjustment) throws DaoException;

    Adjustment get(String invoiceId, String paymentId, String adjustmentId) throws DaoException;

    void updateNotCurrent(Long id) throws DaoException;

}
