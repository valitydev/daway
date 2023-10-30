package dev.vality.daway.dao.dominant.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.dominant.iface.DomainObjectDao;
import dev.vality.daway.domain.Tables;
import dev.vality.daway.domain.tables.pojos.PayoutMethod;
import dev.vality.daway.domain.tables.records.PayoutMethodRecord;
import dev.vality.daway.exception.DaoException;
import org.jooq.Query;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class PayoutMethodDaoImpl extends AbstractGenericDao implements DomainObjectDao<PayoutMethod, String> {

    public PayoutMethodDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long save(PayoutMethod payoutMethod) throws DaoException {
        PayoutMethodRecord payoutMethodRecord = getDslContext().newRecord(Tables.PAYOUT_METHOD, payoutMethod);
        Query query = getDslContext().insertInto(Tables.PAYOUT_METHOD).set(payoutMethodRecord).returning(Tables.PAYOUT_METHOD.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeOne(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public void updateNotCurrent(String payoutMethodId) throws DaoException {
        Query query = getDslContext().update(Tables.PAYOUT_METHOD).set(Tables.PAYOUT_METHOD.CURRENT, false)
                .where(Tables.PAYOUT_METHOD.PAYOUT_METHOD_REF_ID.eq(payoutMethodId).and(Tables.PAYOUT_METHOD.CURRENT));
        executeOne(query);
    }
}
