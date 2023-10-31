package dev.vality.daway.dao.deposit.iface;

import dev.vality.dao.GenericDao;
import dev.vality.daway.domain.tables.pojos.Deposit;
import dev.vality.daway.exception.DaoException;

import java.util.Optional;

public interface DepositDao extends GenericDao {

    Optional<Long> save(Deposit deposit) throws DaoException;

    Deposit get(String depositId) throws DaoException;

    void updateNotCurrent(Long depositId) throws DaoException;

}
