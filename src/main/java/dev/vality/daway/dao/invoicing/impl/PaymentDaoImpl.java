package dev.vality.daway.dao.invoicing.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.invoicing.iface.PaymentDao;
import dev.vality.daway.domain.tables.pojos.Payment;
import dev.vality.daway.domain.tables.records.PaymentRecord;
import dev.vality.daway.exception.DaoException;
import dev.vality.daway.exception.NotFoundException;
import dev.vality.mapper.RecordRowMapper;
import org.jooq.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static dev.vality.daway.domain.tables.Payment.PAYMENT;

@Component
public class PaymentDaoImpl extends AbstractGenericDao implements PaymentDao {

    private final RowMapper<Payment> paymentRowMapper;

    @Autowired
    public PaymentDaoImpl(DataSource dataSource) {
        super(dataSource);
        paymentRowMapper = new RecordRowMapper<>(PAYMENT, Payment.class);
    }

    @Override
    public void saveBatch(List<Payment> payments) throws DaoException {
        List<Query> queries = payments.stream()
                .map(payment -> getDslContext().newRecord(PAYMENT, payment))
                .map(this::prepareInsertQuery)
                .collect(Collectors.toList());
        batchExecute(queries);
    }

    @NotNull
    @Override
    public Payment get(String invoiceId, String paymentId) throws DaoException {
        Query query = getDslContext().selectFrom(PAYMENT)
                .where(PAYMENT.INVOICE_ID.eq(invoiceId)
                        .and(PAYMENT.PAYMENT_ID.eq(paymentId)));
        return Optional.ofNullable(fetchOne(query, paymentRowMapper))
                .orElseThrow(() -> new NotFoundException(
                        String.format("Payment not found, invoiceId='%s', paymentId='%s'", invoiceId, paymentId)));
    }

    private Query prepareInsertQuery(PaymentRecord paymentRecord) {
        return getDslContext().insertInto(PAYMENT)
                .set(paymentRecord)
                .onConflict(
                        PAYMENT.INVOICE_ID,
                        PAYMENT.PAYMENT_ID,
                        PAYMENT.SEQUENCE_ID,
                        PAYMENT.CHANGE_ID
                )
                .doNothing();
    }

}
