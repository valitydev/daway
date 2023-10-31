package dev.vality.daway.dao.dominant.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.dominant.iface.DominantDao;
import dev.vality.daway.domain.Tables;
import dev.vality.daway.exception.DaoException;
import org.jooq.Query;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DominantDaoImpl extends AbstractGenericDao implements DominantDao {

    public DominantDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long getLastVersionId() throws DaoException {
        Query query = getDslContext()
                .select(Tables.DOMINANT_LAST_VERSION_ID.VERSION_ID)
                .from(Tables.DOMINANT_LAST_VERSION_ID);
        return fetchOne(query, Long.class);
    }

    @Override
    public void updateLastVersionId(Long versionId) throws DaoException {
        Query query = getDslContext().update(Tables.DOMINANT_LAST_VERSION_ID)
                .set(Tables.DOMINANT_LAST_VERSION_ID.VERSION_ID, versionId);
        executeOne(query);
    }
}
