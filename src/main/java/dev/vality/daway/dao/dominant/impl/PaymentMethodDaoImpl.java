package dev.vality.daway.dao.dominant.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.dominant.iface.DomainObjectDao;
import dev.vality.daway.domain.Tables;
import dev.vality.daway.domain.tables.pojos.PaymentMethod;
import dev.vality.daway.domain.tables.records.PaymentMethodRecord;
import dev.vality.daway.exception.DaoException;
import org.jooq.Query;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class PaymentMethodDaoImpl extends AbstractGenericDao implements DomainObjectDao<PaymentMethod, String> {

    public PaymentMethodDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long save(PaymentMethod paymentMethod) throws DaoException {
        PaymentMethodRecord paymentMethodRecord = getDslContext().newRecord(Tables.PAYMENT_METHOD, paymentMethod);
        Query query = getDslContext().insertInto(Tables.PAYMENT_METHOD).set(paymentMethodRecord).returning(Tables.PAYMENT_METHOD.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeOne(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public void updateNotCurrent(String paymentMethodId) throws DaoException {
        Query query = getDslContext().update(Tables.PAYMENT_METHOD).set(Tables.PAYMENT_METHOD.CURRENT, false)
                .where(Tables.PAYMENT_METHOD.PAYMENT_METHOD_REF_ID.eq(paymentMethodId).and(Tables.PAYMENT_METHOD.CURRENT));
        execute(query);
    }
}
