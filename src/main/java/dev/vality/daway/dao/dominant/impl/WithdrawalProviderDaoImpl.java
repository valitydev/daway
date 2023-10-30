package dev.vality.daway.dao.dominant.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.dominant.iface.DomainObjectDao;
import dev.vality.daway.domain.Tables;
import dev.vality.daway.domain.tables.pojos.WithdrawalProvider;
import dev.vality.daway.domain.tables.records.WithdrawalProviderRecord;
import dev.vality.daway.exception.DaoException;
import org.jooq.Query;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class WithdrawalProviderDaoImpl extends AbstractGenericDao
        implements DomainObjectDao<WithdrawalProvider, Integer> {

    public WithdrawalProviderDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long save(WithdrawalProvider provider) throws DaoException {
        WithdrawalProviderRecord withdrawalProviderRecord = getDslContext().newRecord(Tables.WITHDRAWAL_PROVIDER, provider);
        Query query = getDslContext().insertInto(Tables.WITHDRAWAL_PROVIDER).set(withdrawalProviderRecord)
                .returning(Tables.WITHDRAWAL_PROVIDER.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeOne(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public void updateNotCurrent(Integer providerId) throws DaoException {
        Query query = getDslContext().update(Tables.WITHDRAWAL_PROVIDER).set(Tables.WITHDRAWAL_PROVIDER.CURRENT, false)
                .where(Tables.WITHDRAWAL_PROVIDER.WITHDRAWAL_PROVIDER_REF_ID.eq(providerId).and(Tables.WITHDRAWAL_PROVIDER.CURRENT));
        executeOne(query);
    }
}
