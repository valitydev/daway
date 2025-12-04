package dev.vality.daway.dao.dominant.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.dominant.iface.DomainObjectDao;
import dev.vality.daway.domain.Tables;
import dev.vality.daway.domain.tables.pojos.Proxy;
import dev.vality.daway.domain.tables.records.ProxyRecord;
import dev.vality.daway.exception.DaoException;
import org.jooq.Query;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Optional;

@Component
public class ProxyDaoImpl extends AbstractGenericDao implements DomainObjectDao<Proxy, Integer> {

    public ProxyDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long save(Proxy proxy) throws DaoException {
        ProxyRecord proxyRecord = getDslContext().newRecord(Tables.PROXY, proxy);
        Query query = getDslContext().insertInto(Tables.PROXY)
                .set(proxyRecord)
                .onConflict(Tables.PROXY.PROXY_REF_ID, Tables.PROXY.VERSION_ID)
                .doNothing()
                .returning(Tables.PROXY.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        execute(query, keyHolder);
        return Optional.ofNullable(keyHolder.getKey()).map(Number::longValue).orElse(null);
    }

    @Override
    public void updateNotCurrent(Integer proxyId) throws DaoException {
        Query query = getDslContext().update(Tables.PROXY).set(Tables.PROXY.CURRENT, false)
                .where(Tables.PROXY.PROXY_REF_ID.eq(proxyId).and(Tables.PROXY.CURRENT));
        executeOne(query);
    }
}
