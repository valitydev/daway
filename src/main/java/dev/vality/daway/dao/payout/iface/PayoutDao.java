package dev.vality.daway.dao.payout.iface;

import dev.vality.dao.GenericDao;
import dev.vality.daway.domain.tables.pojos.Payout;
import dev.vality.daway.exception.DaoException;

import java.util.Optional;

public interface PayoutDao extends GenericDao {

    Optional<Long> save(Payout payout) throws DaoException;

    Payout get(String payoutId) throws DaoException;

    void updateNotCurrent(Long id) throws DaoException;
}
