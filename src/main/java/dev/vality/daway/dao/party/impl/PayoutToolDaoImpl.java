package dev.vality.daway.dao.party.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.party.iface.PayoutToolDao;
import dev.vality.daway.domain.tables.pojos.PayoutTool;
import dev.vality.daway.exception.DaoException;
import dev.vality.mapper.RecordRowMapper;
import org.jooq.Query;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

import static dev.vality.daway.domain.Tables.PAYOUT_TOOL;

@Component
public class PayoutToolDaoImpl extends AbstractGenericDao implements PayoutToolDao {

    private final RowMapper<PayoutTool> payoutToolRowMapper;

    public PayoutToolDaoImpl(DataSource dataSource) {
        super(dataSource);
        this.payoutToolRowMapper = new RecordRowMapper<>(PAYOUT_TOOL, PayoutTool.class);
    }

    @Override
    public void save(List<PayoutTool> payoutToolList) throws DaoException {
        List<Query> queries = payoutToolList.stream()
                .map(payoutTool -> getDslContext().newRecord(PAYOUT_TOOL, payoutTool))
                .map(payoutToolRecord -> getDslContext().insertInto(PAYOUT_TOOL).set(payoutToolRecord))
                .collect(Collectors.toList());
        batchExecute(queries);
    }

    @Override
    public List<PayoutTool> getByCntrctId(Long cntrctId) throws DaoException {
        Query query = getDslContext().selectFrom(PAYOUT_TOOL)
                .where(PAYOUT_TOOL.CNTRCT_ID.eq(cntrctId));
        return fetch(query, payoutToolRowMapper);
    }
}
