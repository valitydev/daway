package dev.vality.daway.dao.withdrawal.session.iface;

import dev.vality.dao.GenericDao;
import dev.vality.daway.domain.tables.pojos.WithdrawalSession;
import dev.vality.daway.exception.DaoException;

import java.time.LocalDateTime;
import java.util.Optional;

public interface WithdrawalSessionDao extends GenericDao {

    Optional<Long> save(WithdrawalSession withdrawalSession) throws DaoException;

    WithdrawalSession get(String sessionId) throws DaoException;

    WithdrawalSession get(String sessionId, LocalDateTime from, LocalDateTime to) throws DaoException;

    void updateNotCurrent(Long sessionId) throws DaoException;

}
