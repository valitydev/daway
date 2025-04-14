package dev.vality.daway.service;

import dev.vality.daway.TestData;
import dev.vality.daway.config.PostgresqlSpringBootITest;
import dev.vality.daway.dao.invoicing.iface.*;
import dev.vality.daway.dao.invoicing.impl.PaymentDaoImpl;
import dev.vality.daway.domain.enums.PaymentChangeType;
import dev.vality.daway.domain.tables.pojos.CashFlow;
import dev.vality.daway.domain.tables.pojos.CashFlowLink;
import dev.vality.daway.domain.tables.pojos.PaymentFee;
import dev.vality.daway.model.CashFlowWrapper;
import dev.vality.daway.model.InvoicingKey;
import dev.vality.daway.model.PaymentWrapper;
import dev.vality.daway.utils.PaymentWrapperTestUtil;
import dev.vality.testcontainers.annotations.util.RandomBeans;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;

import static dev.vality.daway.utils.JdbcUtil.countEntities;
import static dev.vality.daway.utils.JdbcUtil.countPaymentEntity;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@PostgresqlSpringBootITest
class PaymentWrapperServiceTest {

    @Autowired
    private PaymentWrapperService paymentWrapperService;

    @Autowired
    private PaymentDaoImpl paymentDao;
    @Autowired
    private PaymentStatusInfoDao paymentStatusInfoDao;
    @Autowired
    private PaymentSessionInfoDao paymentSessionInfoDao;
    @Autowired
    private PaymentPayerInfoDao paymentPayerInfoDao;
    @Autowired
    private PaymentAdditionalInfoDao paymentAdditionalInfoDao;
    @Autowired
    private PaymentRecurrentInfoDao paymentRecurrentInfoDao;
    @Autowired
    private PaymentRiskDataDao paymentRiskDataDao;
    @Autowired
    private PaymentFeeDao paymentFeeDao;
    @Autowired
    private PaymentRouteDao paymentRouteDao;
    @Autowired
    private CashFlowLinkDao cashFlowLinkDao;
    @Autowired
    private CashFlowDao cashFlowDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String invoiceIdFirst = "invoiceIdFirst";
    private static final String invoiceIdSecond = "invoiceIdSecond";
    private static final String paymentIdFirst = "paymentIdFirst";
    private static final String paymentIdSecond = "paymentIdSecond";

    @Test
    void processTest() {
        List<PaymentWrapper> paymentWrappers = preparePaymentWrappers();

        paymentWrapperService.save(paymentWrappers);
        assertPaymentWrapperFromDao(paymentWrappers.get(0), invoiceIdFirst, paymentIdFirst);
        assertPaymentWrapperFromDao(paymentWrappers.get(1), invoiceIdSecond, paymentIdSecond);
    }

    @Test
    void duplicationTest() {
        List<PaymentWrapper> paymentWrappers = preparePaymentWrappers();

        paymentWrapperService.save(paymentWrappers);

        paymentWrapperService.save(paymentWrappers);
        assertDuplication(invoiceIdFirst, paymentIdFirst);
        assertDuplication(invoiceIdSecond, paymentIdSecond);
        assertTotalDuplication();
    }

    private List<PaymentWrapper> preparePaymentWrappers() {
        List<PaymentWrapper> paymentWrappers = RandomBeans.randomListOf(2, PaymentWrapper.class);
        paymentWrappers.stream()
                .map(PaymentWrapper::getPayment)
                .forEach(payment -> payment.setEventCreatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)));
        paymentWrappers.forEach(pw -> {
            pw.setCashFlowWrapper(new CashFlowWrapper(
                    RandomBeans.random(CashFlowLink.class),
                    RandomBeans.randomListOf(3, CashFlow.class)
            ));
            pw.getCashFlowWrapper().getCashFlows().forEach(cf -> cf.setObjType(PaymentChangeType.payment));
            PaymentWrapperTestUtil.setCurrent(pw, true);
        });
        PaymentWrapperTestUtil.setInvoiceIdAndPaymentId(paymentWrappers.get(0), invoiceIdFirst, paymentIdFirst);
        PaymentWrapperTestUtil.setInvoiceIdAndPaymentId(paymentWrappers.get(1), invoiceIdSecond, paymentIdSecond);
        return paymentWrappers;
    }

    private void assertPaymentWrapperFromDao(PaymentWrapper expected, String invoiceId, String paymentId) {
        assertEquals(expected.getPayment(), paymentDao.get(invoiceId, paymentId));
        assertEquals(expected.getPaymentStatusInfo(), paymentStatusInfoDao.get(invoiceId, paymentId));
        assertEquals(expected.getPaymentPayerInfo(), paymentPayerInfoDao.get(invoiceId, paymentId));
        assertEquals(expected.getPaymentAdditionalInfo(), paymentAdditionalInfoDao.get(invoiceId, paymentId));
        assertEquals(expected.getPaymentRecurrentInfo(), paymentRecurrentInfoDao.get(invoiceId, paymentId));
        assertEquals(expected.getPaymentRiskData(), paymentRiskDataDao.get(invoiceId, paymentId));
        assertEquals(expected.getPaymentFee(), paymentFeeDao.get(invoiceId, paymentId));
        assertEquals(expected.getPaymentRoute(), paymentRouteDao.get(invoiceId, paymentId));
        assertEquals(expected.getPaymentSessionInfo(), paymentSessionInfoDao.get(invoiceId, paymentId));
        assertEquals(expected.getCashFlowWrapper().getCashFlowLink(), cashFlowLinkDao.get(invoiceId, paymentId));
        assertEquals(
                new HashSet<>(expected.getCashFlowWrapper().getCashFlows()),
                new HashSet<>(cashFlowDao.getByObjId(expected.getCashFlowWrapper().getCashFlowLink().getId(),
                        PaymentChangeType.payment))
        );
    }

    private void assertDuplication(String invoiceId, String paymentId) {
        assertEquals(1, countPaymentEntity(jdbcTemplate, "payment", invoiceId, paymentId, false));
        assertEquals(1, countPaymentEntity(jdbcTemplate, "payment_status_info", invoiceId, paymentId, false));
        assertEquals(1, countPaymentEntity(jdbcTemplate, "payment_payer_info", invoiceId, paymentId, false));
        assertEquals(1, countPaymentEntity(jdbcTemplate, "payment_additional_info", invoiceId, paymentId, false));
        assertEquals(1, countPaymentEntity(jdbcTemplate, "payment_recurrent_info", invoiceId, paymentId, false));
        assertEquals(1, countPaymentEntity(jdbcTemplate, "payment_risk_data", invoiceId, paymentId, false));
        assertEquals(1, countPaymentEntity(jdbcTemplate, "payment_fee", invoiceId, paymentId, false));
        assertEquals(1, countPaymentEntity(jdbcTemplate, "payment_route", invoiceId, paymentId, false));
        assertEquals(1, countPaymentEntity(jdbcTemplate, "cash_flow_link", invoiceId, paymentId, false));
        assertEquals(1, countPaymentEntity(jdbcTemplate, "payment_session_info", invoiceId, paymentId, false));
    }

    private void assertTotalDuplication() {
        assertEquals(2, countEntities(jdbcTemplate, "payment"));
        assertEquals(2, countEntities(jdbcTemplate, "payment_status_info"));
        assertEquals(2, countEntities(jdbcTemplate, "payment_payer_info"));
        assertEquals(2, countEntities(jdbcTemplate, "payment_additional_info"));
        assertEquals(2, countEntities(jdbcTemplate, "payment_recurrent_info"));
        assertEquals(2, countEntities(jdbcTemplate, "payment_risk_data"));
        assertEquals(2, countEntities(jdbcTemplate, "payment_fee"));
        assertEquals(2, countEntities(jdbcTemplate, "payment_route"));
        assertEquals(2, countEntities(jdbcTemplate, "cash_flow_link"));
        assertEquals(6, countEntities(jdbcTemplate, "cash_flow"));
        assertEquals(2, countEntities(jdbcTemplate, "payment_session_info"));
    }

    @Test
    void testPaymentFeeWithEmptyWrapper() {
        PaymentFee paymentFee = new PaymentFee();
        paymentFee.setPaymentId(TestData.randomString());
        paymentFee.setInvoiceId(TestData.randomString());
        paymentFee.setEventCreatedAt(LocalDateTime.now());
        PaymentWrapper wrapperWithFee = new PaymentWrapper();
        wrapperWithFee.setPaymentFee(paymentFee);
        wrapperWithFee.setKey(InvoicingKey.builder()
                .paymentId(paymentFee.getPaymentId())
                .invoiceId(paymentFee.getInvoiceId())
                .build());
        PaymentWrapper emptyWrapper = new PaymentWrapper();

        assertDoesNotThrow(() -> paymentWrapperService.save(List.of(wrapperWithFee, emptyWrapper)));
    }
}
