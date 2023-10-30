package dev.vality.daway.dao.invoicing.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.invoicing.iface.RefundDao;
import dev.vality.daway.domain.enums.PaymentChangeType;
import dev.vality.daway.domain.tables.pojos.Refund;
import dev.vality.daway.domain.tables.records.RefundRecord;
import dev.vality.daway.exception.DaoException;
import dev.vality.daway.exception.NotFoundException;
import dev.vality.mapper.RecordRowMapper;
import org.jooq.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.util.Optional;

import static dev.vality.daway.domain.tables.Refund.REFUND;

@Component
public class RefundDaoImpl extends AbstractGenericDao implements RefundDao {

    private final RowMapper<Refund> refundRowMapper;

    @Autowired
    public RefundDaoImpl(DataSource dataSource) {
        super(dataSource);
        refundRowMapper = new RecordRowMapper<>(REFUND, Refund.class);
    }

    @Override
    public Optional<Long> save(Refund refund) throws DaoException {
        RefundRecord record = getDslContext().newRecord(REFUND, refund);
        Query query = getDslContext().insertInto(REFUND)
                .set(record)
                .onConflict(REFUND.INVOICE_ID, REFUND.SEQUENCE_ID, REFUND.CHANGE_ID)
                .doNothing()
                .returning(REFUND.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        execute(query, keyHolder);
        return Optional.ofNullable(keyHolder.getKey()).map(Number::longValue);
    }

    @NotNull
    @Override
    public Refund get(String invoiceId, String paymentId, String refundId) throws DaoException {
        Query query = getDslContext().selectFrom(REFUND)
                .where(REFUND.INVOICE_ID.eq(invoiceId)
                        .and(REFUND.PAYMENT_ID.eq(paymentId))
                        .and(REFUND.REFUND_ID.eq(refundId))
                        .and(REFUND.CURRENT));

        return Optional.ofNullable(fetchOne(query, refundRowMapper))
                .orElseThrow(() -> new NotFoundException(String.format("Refund not found, " +
                        "invoiceId='%s', paymentId='%s', refundId='%s'", invoiceId, paymentId, refundId)));
    }

    @Override
    public void updateCommissions(Long rfndId) throws DaoException {
        MapSqlParameterSource params =
                new MapSqlParameterSource("rfndId", rfndId).addValue("objType", PaymentChangeType.refund.name());
        this.getNamedParameterJdbcTemplate().update(
                """
                        UPDATE dw.refund 
                        SET 
                            fee = (
                                SELECT dw.get_refund_fee(dw.cash_flow.*)
                                FROM dw.cash_flow 
                                WHERE obj_id = :rfndId
                                    AND obj_type = CAST(:objType as dw.payment_change_type)
                                ),
                            provider_fee = (
                                SELECT dw.get_refund_provider_fee(dw.cash_flow.*)
                                FROM dw.cash_flow
                                WHERE obj_id = :rfndId
                                    AND obj_type = CAST(:objType as dw.payment_change_type)
                                ),
                            external_fee = (
                                SELECT dw.get_refund_external_fee(dw.cash_flow.*)
                                FROM dw.cash_flow
                                WHERE obj_id = :rfndId
                                    AND obj_type = CAST(:objType as dw.payment_change_type)
                                )
                        WHERE id = :rfndId""",
                params);
    }

    @Override
    public void updateNotCurrent(Long id) throws DaoException {
        Query query = getDslContext().update(REFUND).set(REFUND.CURRENT, false).where(REFUND.ID.eq(id));
        executeOne(query);
    }
}
