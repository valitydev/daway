package dev.vality.daway.dao.dominant.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.dominant.iface.DomainObjectDao;
import dev.vality.daway.domain.Tables;
import dev.vality.daway.domain.tables.pojos.TradeBloc;
import dev.vality.daway.domain.tables.records.TradeBlocRecord;
import dev.vality.daway.exception.DaoException;
import org.jooq.Query;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class TradeBlocDaoImpl extends AbstractGenericDao implements DomainObjectDao<TradeBloc, String> {

    public TradeBlocDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long save(TradeBloc tradeBloc) throws DaoException {
        TradeBlocRecord tradeBlocRecord = getDslContext().newRecord(Tables.TRADE_BLOC, tradeBloc);
        Query query = getDslContext().insertInto(Tables.TRADE_BLOC).set(tradeBlocRecord).returning(Tables.TRADE_BLOC.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeOne(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public void updateNotCurrent(String tradeBlocId) throws DaoException {
        Query query = getDslContext().update(Tables.TRADE_BLOC).set(Tables.TRADE_BLOC.CURRENT, false)
                .where(Tables.TRADE_BLOC.TRADE_BLOC_REF_ID.eq(tradeBlocId).and(Tables.TRADE_BLOC.CURRENT));
        executeOne(query);
    }
}
