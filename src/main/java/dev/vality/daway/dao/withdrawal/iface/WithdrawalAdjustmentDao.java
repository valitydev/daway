package dev.vality.daway.dao.withdrawal.iface;

import dev.vality.dao.GenericDao;
import dev.vality.daway.domain.tables.pojos.WithdrawalAdjustment;
import dev.vality.daway.exception.DaoException;

import java.util.Optional;

public interface WithdrawalAdjustmentDao extends GenericDao {

    Optional<Long> save(WithdrawalAdjustment withdrawalAdjustment) throws DaoException;

    WithdrawalAdjustment getByIds(String withdrawalId, String withdrawalAdjustmentId) throws DaoException;

    void updateNotCurrent(Long withdrawalAdjustmentId) throws DaoException;

}
