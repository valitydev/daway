package dev.vality.daway.dao.dominant.iface;

import dev.vality.dao.GenericDao;
import dev.vality.daway.exception.DaoException;

public interface DomainObjectDao<T, I> extends GenericDao {

    Long save(T domainObject) throws DaoException;

    void updateNotCurrent(I objectId) throws DaoException;
}
