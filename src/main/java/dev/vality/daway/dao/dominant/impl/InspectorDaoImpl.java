package dev.vality.daway.dao.dominant.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.dominant.iface.DomainObjectDao;
import dev.vality.daway.domain.Tables;
import dev.vality.daway.domain.tables.pojos.Inspector;
import dev.vality.daway.domain.tables.records.InspectorRecord;
import dev.vality.daway.exception.DaoException;
import org.jooq.Query;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class InspectorDaoImpl extends AbstractGenericDao implements DomainObjectDao<Inspector, Integer> {

    public InspectorDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long save(Inspector inspector) throws DaoException {
        InspectorRecord inspectorRecord = getDslContext().newRecord(Tables.INSPECTOR, inspector);
        Query query = getDslContext().insertInto(Tables.INSPECTOR).set(inspectorRecord).returning(Tables.INSPECTOR.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeOne(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public void updateNotCurrent(Integer inspectorId) throws DaoException {
        Query query = getDslContext().update(Tables.INSPECTOR).set(Tables.INSPECTOR.CURRENT, false)
                .where(Tables.INSPECTOR.INSPECTOR_REF_ID.eq(inspectorId).and(Tables.INSPECTOR.CURRENT));
        executeOne(query);
    }
}
