package dev.vality.daway.dao.dominant.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.dominant.iface.DomainObjectDao;
import dev.vality.daway.domain.Tables;
import dev.vality.daway.domain.tables.pojos.Provider;
import dev.vality.daway.domain.tables.records.ProviderRecord;
import dev.vality.daway.exception.DaoException;
import org.jooq.Query;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class ProviderDaoImpl extends AbstractGenericDao implements DomainObjectDao<Provider, Integer> {

    public ProviderDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long save(Provider provider) throws DaoException {
        ProviderRecord providerRecord = getDslContext().newRecord(Tables.PROVIDER, provider);
        Query query = getDslContext().insertInto(Tables.PROVIDER).set(providerRecord).returning(Tables.PROVIDER.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeOne(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public void updateNotCurrent(Integer providerId) throws DaoException {
        Query query = getDslContext().update(Tables.PROVIDER).set(Tables.PROVIDER.CURRENT, false)
                .where(Tables.PROVIDER.PROVIDER_REF_ID.eq(providerId).and(Tables.PROVIDER.CURRENT));
        executeOne(query);
    }
}
