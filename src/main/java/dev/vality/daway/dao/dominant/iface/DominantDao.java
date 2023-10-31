package dev.vality.daway.dao.dominant.iface;

import dev.vality.dao.GenericDao;
import dev.vality.daway.exception.DaoException;

public interface DominantDao extends GenericDao {
    Long getLastVersionId() throws DaoException;
    void updateLastVersionId(Long versionId) throws DaoException;
}
