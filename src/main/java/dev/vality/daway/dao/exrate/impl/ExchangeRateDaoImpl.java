package dev.vality.daway.dao.exrate.impl;

import dev.vality.dao.DaoException;
import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.exrate.iface.ExchangeRateDao;
import dev.vality.daway.domain.tables.pojos.ExRate;
import dev.vality.mapper.RecordRowMapper;
import org.jooq.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

import static dev.vality.daway.domain.tables.ExRate.EX_RATE;

@Component
public class ExchangeRateDaoImpl extends AbstractGenericDao implements ExchangeRateDao {

    private final RowMapper<ExRate> rowMapper;

    @Autowired
    public ExchangeRateDaoImpl(@Qualifier("dataSource") DataSource dataSource) {
        super(dataSource);
        this.rowMapper = new RecordRowMapper<>(EX_RATE, ExRate.class);
    }

    @Override
    public void saveBatch(List<ExRate> exchangeRates) throws DaoException {
        List<Query> queryList = exchangeRates.stream()
                .map(exrate -> getDslContext().newRecord(EX_RATE, exrate))
                .map(record -> (Query) getDslContext().insertInto(EX_RATE).set(record)
                        .onConflict(EX_RATE.EVENT_ID)
                        .doNothing())
                .collect(Collectors.toList());
        batchExecute(queryList);
    }

    @Override
    public ExRate findBySourceSymbolicCode(String symbolicCode) {
        Query query = getDslContext().selectFrom(EX_RATE)
                .where(EX_RATE.SOURCE_CURRENCY_SYMBOLIC_CODE.eq(symbolicCode));
        return fetchOne(query, rowMapper);
    }
}
