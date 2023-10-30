package dev.vality.daway.dao.deposit.adjustment.iface;

import dev.vality.dao.GenericDao;
import dev.vality.daway.domain.tables.pojos.DepositAdjustment;
import dev.vality.daway.exception.DaoException;

import java.util.Optional;

public interface DepositAdjustmentDao extends GenericDao {

    Optional<Long> save(DepositAdjustment deposit) throws DaoException;

    DepositAdjustment get(String depositId, String adjustmentId) throws DaoException;

    void updateNotCurrent(Long depositId) throws DaoException;

}
