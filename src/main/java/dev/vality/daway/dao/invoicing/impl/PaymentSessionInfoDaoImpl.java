package dev.vality.daway.dao.invoicing.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.invoicing.iface.PaymentSessionInfoDao;
import dev.vality.daway.domain.tables.pojos.PaymentSessionInfo;
import dev.vality.daway.domain.tables.records.PaymentSessionInfoRecord;
import dev.vality.daway.exception.DaoException;
import dev.vality.daway.exception.NotFoundException;
import dev.vality.mapper.RecordRowMapper;
import org.jooq.Query;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static dev.vality.daway.domain.tables.PaymentSessionInfo.PAYMENT_SESSION_INFO;

@Component
public class PaymentSessionInfoDaoImpl extends AbstractGenericDao implements PaymentSessionInfoDao {

    private final RowMapper<PaymentSessionInfo> rowMapper;

    public PaymentSessionInfoDaoImpl(DataSource dataSource) {
        super(dataSource);
        this.rowMapper = new RecordRowMapper<>(PAYMENT_SESSION_INFO, PaymentSessionInfo.class);
    }

    @Override
    public void saveBatch(List<PaymentSessionInfo> paymentSessionInfos) throws DaoException {
        List<Query> queries = paymentSessionInfos.stream()
                .map(statusInfo -> getDslContext().newRecord(PAYMENT_SESSION_INFO, statusInfo))
                .map(this::prepareInsertQuery)
                .collect(Collectors.toList());
        batchExecute(queries);
    }

    @Override
    public PaymentSessionInfo get(String invoiceId, String paymentId) throws DaoException {
        Query query = getDslContext().selectFrom(PAYMENT_SESSION_INFO)
                .where(PAYMENT_SESSION_INFO.INVOICE_ID.eq(invoiceId)
                        .and(PAYMENT_SESSION_INFO.PAYMENT_ID.eq(paymentId)));
        return Optional.ofNullable(fetchOne(query, rowMapper)).orElseThrow(() ->
                new NotFoundException(
                        "PaymentPayerInfo not found, invoiceId=" + invoiceId + " paymentId=" + paymentId));
    }


    private Query prepareInsertQuery(PaymentSessionInfoRecord record) {
        return getDslContext().insertInto(PAYMENT_SESSION_INFO)
                .set(record)
                .onConflict(
                        PAYMENT_SESSION_INFO.INVOICE_ID,
                        PAYMENT_SESSION_INFO.PAYMENT_ID,
                        PAYMENT_SESSION_INFO.SEQUENCE_ID,
                        PAYMENT_SESSION_INFO.CHANGE_ID,
                        PAYMENT_SESSION_INFO.EVENT_CREATED_AT
                )
                .doNothing();
    }

}
