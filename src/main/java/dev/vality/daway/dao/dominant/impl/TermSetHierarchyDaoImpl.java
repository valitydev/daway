package dev.vality.daway.dao.dominant.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.dominant.iface.DomainObjectDao;
import dev.vality.daway.domain.Tables;
import dev.vality.daway.domain.tables.pojos.TermSetHierarchy;
import dev.vality.daway.domain.tables.records.TermSetHierarchyRecord;
import dev.vality.daway.exception.DaoException;
import org.jooq.Query;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class TermSetHierarchyDaoImpl extends AbstractGenericDao implements DomainObjectDao<TermSetHierarchy, Integer> {

    public TermSetHierarchyDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long save(TermSetHierarchy termSetHierarchy) throws DaoException {
        TermSetHierarchyRecord termSetHierarchyRecord = getDslContext().newRecord(Tables.TERM_SET_HIERARCHY, termSetHierarchy);
        Query query = getDslContext().insertInto(Tables.TERM_SET_HIERARCHY).set(termSetHierarchyRecord)
                .returning(Tables.TERM_SET_HIERARCHY.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeOne(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public void updateNotCurrent(Integer termSetHierarchyId) throws DaoException {
        Query query = getDslContext().update(Tables.TERM_SET_HIERARCHY).set(Tables.TERM_SET_HIERARCHY.CURRENT, false)
                .where(Tables.TERM_SET_HIERARCHY.TERM_SET_HIERARCHY_REF_ID.eq(termSetHierarchyId)
                        .and(Tables.TERM_SET_HIERARCHY.CURRENT));
        executeOne(query);
    }
}
