package com.rbkmoney.newway.dao.invoicing.impl;

import com.rbkmoney.newway.dao.common.impl.AbstractGenericDao;
import com.rbkmoney.newway.dao.common.mapper.RecordRowMapper;
import com.rbkmoney.newway.dao.invoicing.iface.PaymentDao;
import com.rbkmoney.newway.domain.enums.PaymentChangeType;
import com.rbkmoney.newway.domain.tables.pojos.Payment;
import com.rbkmoney.newway.domain.tables.records.PaymentRecord;
import com.rbkmoney.newway.exception.DaoException;
import org.jooq.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import java.util.Optional;

import static com.rbkmoney.newway.domain.Tables.PAYMENT;

@Component
public class PaymentDaoImpl extends AbstractGenericDao implements PaymentDao {

    private final RowMapper<Payment> paymentRowMapper;

    @Autowired
    public PaymentDaoImpl(DataSource dataSource) {
        super(dataSource);
        paymentRowMapper = new RecordRowMapper<>(PAYMENT, Payment.class);
    }

    @Override
    public Long save(Payment payment) throws DaoException {
        PaymentRecord paymentRecord = getDslContext().newRecord(PAYMENT, payment);
        Query query = getDslContext().insertInto(PAYMENT)
                .set(paymentRecord)
                .onConflict(PAYMENT.INVOICE_ID, PAYMENT.SEQUENCE_ID, PAYMENT.CHANGE_ID)
                .doNothing()
                .returning(PAYMENT.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeWithReturn(query, keyHolder);
        return Optional.ofNullable(keyHolder.getKey()).map(Number::longValue).orElse(null);
    }

    @Override
    public void updateCommissions(Long pmntId) throws DaoException {
        MapSqlParameterSource params = new MapSqlParameterSource("pmntId", pmntId).addValue("objType", PaymentChangeType.payment.name());
        this.getNamedParameterJdbcTemplate().update(
                "UPDATE nw.payment SET fee = (SELECT nw.get_payment_fee(nw.cash_flow.*) FROM nw.cash_flow WHERE obj_id = :pmntId AND obj_type = CAST(:objType as nw.payment_change_type)), " +
                        "provider_fee = (SELECT nw.get_payment_provider_fee(nw.cash_flow.*) FROM nw.cash_flow WHERE obj_id = :pmntId AND obj_type = CAST(:objType as nw.payment_change_type)), " +
                        "external_fee = (SELECT nw.get_payment_external_fee(nw.cash_flow.*) FROM nw.cash_flow WHERE obj_id = :pmntId AND obj_type = CAST(:objType as nw.payment_change_type)), " +
                        "guarantee_deposit = (SELECT nw.get_payment_guarantee_deposit(nw.cash_flow.*) FROM nw.cash_flow WHERE obj_id = :pmntId AND obj_type = CAST(:objType as nw.payment_change_type)) " +
                        "WHERE  id = :pmntId",
                params);
    }

    @Override
    public Payment get(String invoiceId, String paymentId) throws DaoException {
        Query query = getDslContext().selectFrom(PAYMENT)
                .where(PAYMENT.INVOICE_ID.eq(invoiceId).and(PAYMENT.PAYMENT_ID.eq(paymentId)).and(PAYMENT.CURRENT));

        return fetchOne(query, paymentRowMapper);
    }

    @Override
    public void updateNotCurrent(String invoiceId, String paymentId) throws DaoException {
        Query query = getDslContext().update(PAYMENT).set(PAYMENT.CURRENT, false)
                .where(PAYMENT.INVOICE_ID.eq(invoiceId).and(PAYMENT.PAYMENT_ID.eq(paymentId).and(PAYMENT.CURRENT)));
        executeOne(query);
    }
}
