package dev.vality.daway.dao.withdrawal.iface;

import dev.vality.dao.GenericDao;
import dev.vality.daway.domain.tables.pojos.WithdrawalCashChange;
import dev.vality.daway.exception.DaoException;

public interface WithdrawalCashChangeDao extends GenericDao {

    void save(WithdrawalCashChange withdrawalCashChange) throws DaoException;

    void switchCurrent(String withdrawalId) throws DaoException;

}
