package dev.vality.daway.dao.identity.iface;

import dev.vality.dao.GenericDao;
import dev.vality.daway.domain.tables.pojos.Identity;
import dev.vality.daway.exception.DaoException;

import java.util.Optional;

public interface IdentityDao extends GenericDao {

    Optional<Long> save(Identity identity) throws DaoException;

    Identity get(String identityId) throws DaoException;

    void updateNotCurrent(Long identityId) throws DaoException;

}
