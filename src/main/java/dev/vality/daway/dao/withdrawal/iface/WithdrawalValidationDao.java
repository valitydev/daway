package dev.vality.daway.dao.withdrawal.iface;

import dev.vality.dao.GenericDao;
import dev.vality.daway.domain.tables.pojos.WithdrawalValidation;
import dev.vality.daway.exception.DaoException;

import java.util.Optional;

public interface WithdrawalValidationDao extends GenericDao {

    Optional<Long> save(WithdrawalValidation withdrawalValidation) throws DaoException;

}
