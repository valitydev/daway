package dev.vality.daway.dao.partition;

import dev.vality.daway.config.PostgresqlJooqSpringBootITest;
import dev.vality.daway.dao.invoicing.iface.*;
import dev.vality.daway.dao.invoicing.impl.*;
import dev.vality.daway.domain.tables.pojos.*;
import dev.vality.daway.model.InvoicingKey;
import dev.vality.testcontainers.annotations.util.RandomBeans;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static dev.vality.daway.domain.tables.Invoice.INVOICE;
import static dev.vality.daway.domain.tables.InvoiceStatusInfo.INVOICE_STATUS_INFO;
import static dev.vality.daway.domain.tables.Payment.PAYMENT;
import static dev.vality.daway.domain.tables.PaymentAdditionalInfo.PAYMENT_ADDITIONAL_INFO;
import static dev.vality.daway.domain.tables.PaymentFee.PAYMENT_FEE;
import static dev.vality.daway.domain.tables.PaymentPayerInfo.PAYMENT_PAYER_INFO;
import static dev.vality.daway.domain.tables.PaymentRiskData.PAYMENT_RISK_DATA;
import static dev.vality.daway.domain.tables.PaymentRoute.PAYMENT_ROUTE;
import static dev.vality.daway.domain.tables.PaymentStatusInfo.PAYMENT_STATUS_INFO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@ContextConfiguration(classes = {
        PaymentDaoImpl.class, PaymentStatusInfoDaoImpl.class, PaymentPayerInfoDaoImpl.class,
        PaymentAdditionalInfoDaoImpl.class, PaymentRiskDataDaoImpl.class, PaymentFeeDaoImpl.class,
        PaymentRouteDaoImpl.class, InvoiceDaoImpl.class, InvoiceStatusInfoDaoImpl.class})
@PostgresqlJooqSpringBootITest
@Sql(scripts = {"classpath:sql/partition/payment.sql", "classpath:sql/partition/payment_status_info.sql",
        "classpath:sql/partition/payment_payer_info.sql", "classpath:sql/partition/payment_additional_info.sql",
        "classpath:sql/partition/payment_risk_data.sql", "classpath:sql/partition/payment_fee.sql",
        "classpath:sql/partition/payment_route.sql", "classpath:sql/partition/invoice.sql",
        "classpath:sql/partition/invoice_status_info.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class DaoTests {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private PaymentDao paymentDao;
    @Autowired
    private PaymentStatusInfoDao paymentStatusInfoDao;
    @Autowired
    private PaymentPayerInfoDao paymentPayerInfoDao;
    @Autowired
    private PaymentAdditionalInfoDao paymentAdditionalInfoDao;
    @Autowired
    private PaymentRiskDataDao paymentRiskDataDao;
    @Autowired
    private PaymentFeeDao paymentFeeDao;
    @Autowired
    private PaymentRouteDao paymentRouteDao;
    @Autowired
    private InvoiceDao invoiceDao;
    @Autowired
    private InvoiceStatusInfoDao invoiceStatusInfoDao;


    @BeforeEach
    void setUp() {
        dslContext.deleteFrom(PAYMENT).execute();
        dslContext.deleteFrom(PAYMENT_ADDITIONAL_INFO).execute();
        dslContext.deleteFrom(PAYMENT_RISK_DATA).execute();
        dslContext.deleteFrom(PAYMENT_FEE).execute();
        dslContext.deleteFrom(PAYMENT_ROUTE).execute();
        dslContext.deleteFrom(PAYMENT_STATUS_INFO).execute();
        dslContext.deleteFrom(PAYMENT_PAYER_INFO).execute();
        dslContext.deleteFrom(INVOICE).execute();
        dslContext.deleteFrom(INVOICE_STATUS_INFO).execute();
    }

    @Test
    void invoiceDaoTest() {
        List<Invoice> invoices = RandomBeans.randomListOf(3, Invoice.class);
        invoiceDao.saveBatch(invoices);
        assertEquals(invoices.get(0), invoiceDao.get(invoices.get(0).getInvoiceId()));
        assertEquals(invoices.get(1), invoiceDao.get(invoices.get(1).getInvoiceId()));
        assertEquals(invoices.get(2), invoiceDao.get(invoices.get(2).getInvoiceId()));
    }


    @Test
    void invoiceStatusInfoDaoTest() {
        List<InvoiceStatusInfo> statusInfos =
                RandomBeans.randomListOf(3, InvoiceStatusInfo.class);
        statusInfos.forEach(status -> status.setCurrent(true));
        invoiceStatusInfoDao.saveBatch(statusInfos);
        assertEquals(statusInfos.get(0), invoiceStatusInfoDao.get(statusInfos.get(0).getInvoiceId()));
        assertEquals(statusInfos.get(1), invoiceStatusInfoDao.get(statusInfos.get(1).getInvoiceId()));
        assertEquals(statusInfos.get(2), invoiceStatusInfoDao.get(statusInfos.get(2).getInvoiceId()));

        InvoiceStatusInfo statusInfo = RandomBeans.random(InvoiceStatusInfo.class);
        InvoiceStatusInfo initialStatusInfo = statusInfos.get(0);
        statusInfo.setInvoiceId(initialStatusInfo.getInvoiceId());
        statusInfo.setCurrent(false);
        statusInfo.setId(initialStatusInfo.getId() + 1);
        invoiceStatusInfoDao.saveBatch(List.of(statusInfo));
        assertNotEquals(statusInfo, initialStatusInfo);
        assertEquals(initialStatusInfo, invoiceStatusInfoDao.get(initialStatusInfo.getInvoiceId()));
        invoiceStatusInfoDao.switchCurrent(Set.of(statusInfo.getInvoiceId()));
        statusInfo.setCurrent(true);
        assertEquals(statusInfo, invoiceStatusInfoDao.get(initialStatusInfo.getInvoiceId()));
    }

    @Test
    void paymentDaoTest() {
        Payment first = RandomBeans.random(Payment.class);
        first.setId(1L);
        first.setEventCreatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        Payment second = RandomBeans.random(Payment.class);
        second.setId(2L);
        second.setEventCreatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        paymentDao.saveBatch(Arrays.asList(first, second));
        assertEquals(first, paymentDao.get(first.getInvoiceId(), first.getPaymentId()));
        assertEquals(second, paymentDao.get(second.getInvoiceId(), second.getPaymentId()));
    }

    @Test
    void paymentStatusInfoDaoTest() {
        List<PaymentStatusInfo> statusInfos = RandomBeans.randomListOf(2, PaymentStatusInfo.class);
        statusInfos.forEach(statusInfo -> statusInfo.setCurrent(true));
        paymentStatusInfoDao.saveBatch(statusInfos);
        PaymentStatusInfo first = statusInfos.get(0);
        assertEquals(first, paymentStatusInfoDao.get(first.getInvoiceId(), first.getPaymentId()));
        PaymentStatusInfo second = statusInfos.get(1);
        assertEquals(second, paymentStatusInfoDao.get(second.getInvoiceId(), second.getPaymentId()));

        PaymentStatusInfo third = RandomBeans.random(PaymentStatusInfo.class);
        third.setId(first.getId() + 1);
        third.setCurrent(false);
        third.setInvoiceId(first.getInvoiceId());
        third.setPaymentId(first.getPaymentId());
        paymentStatusInfoDao.saveBatch(List.of(third));
        assertEquals(first, paymentStatusInfoDao.get(third.getInvoiceId(), third.getPaymentId()));
        paymentStatusInfoDao.switchCurrent(Set.of(InvoicingKey.buildKey(third.getInvoiceId(), third.getPaymentId())));
        third.setCurrent(true);
        assertEquals(third, paymentStatusInfoDao.get(third.getInvoiceId(), third.getPaymentId()));
    }

    @Test
    void paymentPayerInfoDaoTest() {
        PaymentPayerInfo first = RandomBeans.random(PaymentPayerInfo.class);
        first.setId(1L);
        PaymentPayerInfo second = RandomBeans.random(PaymentPayerInfo.class);
        second.setId(2L);
        paymentPayerInfoDao.saveBatch(Arrays.asList(first, second));
        assertEquals(first, paymentPayerInfoDao.get(first.getInvoiceId(), first.getPaymentId()));
        assertEquals(second, paymentPayerInfoDao.get(second.getInvoiceId(), second.getPaymentId()));
    }

    @Test
    void paymentAdditionalInfoDaoTest() {
        List<PaymentAdditionalInfo> list = RandomBeans.randomListOf(2, PaymentAdditionalInfo.class);
        list.forEach(statusInfo -> statusInfo.setCurrent(true));
        paymentAdditionalInfoDao.saveBatch(list);
        PaymentAdditionalInfo first = list.get(0);
        assertEquals(first, paymentAdditionalInfoDao.get(first.getInvoiceId(), first.getPaymentId()));
        PaymentAdditionalInfo second = list.get(1);
        assertEquals(second, paymentAdditionalInfoDao.get(second.getInvoiceId(), second.getPaymentId()));

        PaymentAdditionalInfo third = RandomBeans.random(PaymentAdditionalInfo.class);
        third.setId(first.getId() + 1);
        third.setCurrent(false);
        third.setInvoiceId(first.getInvoiceId());
        third.setPaymentId(first.getPaymentId());
        paymentAdditionalInfoDao.saveBatch(List.of(third));
        assertEquals(first, paymentAdditionalInfoDao.get(third.getInvoiceId(), third.getPaymentId()));
        paymentAdditionalInfoDao.switchCurrent(Set.of(InvoicingKey.buildKey(third.getInvoiceId(), third.getPaymentId())));
        third.setCurrent(true);
        assertEquals(third, paymentAdditionalInfoDao.get(third.getInvoiceId(), third.getPaymentId()));
    }

    @Test
    void paymentRiskDataDaoTest() {
        List<PaymentRiskData> list = RandomBeans.randomListOf(2, PaymentRiskData.class);
        list.forEach(statusInfo -> statusInfo.setCurrent(true));
        paymentRiskDataDao.saveBatch(list);
        PaymentRiskData first = list.get(0);
        assertEquals(first, paymentRiskDataDao.get(first.getInvoiceId(), first.getPaymentId()));
        PaymentRiskData second = list.get(1);
        assertEquals(second, paymentRiskDataDao.get(second.getInvoiceId(), second.getPaymentId()));

        PaymentRiskData third = RandomBeans.random(PaymentRiskData.class);
        third.setId(first.getId() + 1);
        third.setCurrent(false);
        third.setInvoiceId(first.getInvoiceId());
        third.setPaymentId(first.getPaymentId());
        paymentRiskDataDao.saveBatch(List.of(third));
        assertEquals(first, paymentRiskDataDao.get(third.getInvoiceId(), third.getPaymentId()));
        paymentRiskDataDao.switchCurrent(Set.of(InvoicingKey.buildKey(third.getInvoiceId(), third.getPaymentId())));
        third.setCurrent(true);
        assertEquals(third, paymentRiskDataDao.get(third.getInvoiceId(), third.getPaymentId()));
    }

    @Test
    void paymentFeeDaoTest() {
        List<PaymentFee> list = RandomBeans.randomListOf(2, PaymentFee.class);
        list.forEach(statusInfo -> statusInfo.setCurrent(true));
        paymentFeeDao.saveBatch(list);
        PaymentFee first = list.get(0);
        assertEquals(first, paymentFeeDao.get(first.getInvoiceId(), first.getPaymentId()));
        PaymentFee second = list.get(1);
        assertEquals(second, paymentFeeDao.get(second.getInvoiceId(), second.getPaymentId()));

        PaymentFee third = RandomBeans.random(PaymentFee.class);
        third.setId(first.getId() + 1);
        third.setCurrent(false);
        third.setInvoiceId(first.getInvoiceId());
        third.setPaymentId(first.getPaymentId());
        paymentFeeDao.saveBatch(List.of(third));
        assertEquals(first, paymentFeeDao.get(third.getInvoiceId(), third.getPaymentId()));
        paymentFeeDao.switchCurrent(Set.of(InvoicingKey.buildKey(third.getInvoiceId(), third.getPaymentId())));
        third.setCurrent(true);
        assertEquals(third, paymentFeeDao.get(third.getInvoiceId(), third.getPaymentId()));
    }

    @Test
    void paymentRouteDaoTest() {
        List<PaymentRoute> list = RandomBeans.randomListOf(2, PaymentRoute.class);
        list.forEach(statusInfo -> statusInfo.setCurrent(true));
        paymentRouteDao.saveBatch(list);
        PaymentRoute first = list.get(0);
        assertEquals(first, paymentRouteDao.get(first.getInvoiceId(), first.getPaymentId()));
        PaymentRoute second = list.get(1);
        assertEquals(second, paymentRouteDao.get(second.getInvoiceId(), second.getPaymentId()));

        PaymentRoute third = RandomBeans.random(PaymentRoute.class);
        third.setId(first.getId() + 1);
        third.setCurrent(false);
        third.setInvoiceId(first.getInvoiceId());
        third.setPaymentId(first.getPaymentId());
        paymentRouteDao.saveBatch(List.of(third));
        assertEquals(first, paymentRouteDao.get(third.getInvoiceId(), third.getPaymentId()));
        paymentRouteDao.switchCurrent(Set.of(InvoicingKey.buildKey(third.getInvoiceId(), third.getPaymentId())));
        third.setCurrent(true);
        assertEquals(third, paymentRouteDao.get(third.getInvoiceId(), third.getPaymentId()));
    }
}
