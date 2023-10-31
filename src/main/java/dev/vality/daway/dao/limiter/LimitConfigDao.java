package dev.vality.daway.dao.limiter;

import dev.vality.dao.GenericDao;
import dev.vality.daway.domain.tables.pojos.LimitConfig;
import dev.vality.daway.exception.DaoException;

import java.util.Optional;

public interface LimitConfigDao extends GenericDao {

    Optional<Long> save(LimitConfig limitConfig) throws DaoException;

    void updateNotCurrent(Long id) throws DaoException;

}
