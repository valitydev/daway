package dev.vality.daway.dao.dominant.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.dominant.iface.DomainObjectDao;
import dev.vality.daway.domain.Tables;
import dev.vality.daway.domain.tables.pojos.Currency;
import dev.vality.daway.domain.tables.records.CurrencyRecord;
import dev.vality.daway.exception.DaoException;
import org.jooq.Query;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class CurrencyDaoImpl extends AbstractGenericDao implements DomainObjectDao<Currency, String> {

    public CurrencyDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long save(Currency currency) throws DaoException {
        CurrencyRecord currencyRecord = getDslContext().newRecord(Tables.CURRENCY, currency);
        Query query = getDslContext().insertInto(Tables.CURRENCY)
                .set(currencyRecord)
                .onConflict(Tables.CURRENCY.CURRENCY_REF_ID, Tables.CURRENCY.VERSION_ID)
                .doNothing()
                .returning(Tables.CURRENCY.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeOne(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public void updateNotCurrent(String currencyId) throws DaoException {
        Query query = getDslContext().update(Tables.CURRENCY).set(Tables.CURRENCY.CURRENT, false)
                .where(Tables.CURRENCY.CURRENCY_REF_ID.eq(currencyId).and(Tables.CURRENCY.CURRENT));
        executeOne(query);
    }
}
