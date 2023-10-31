package dev.vality.daway;

import dev.vality.damsel.domain.PaymentRoute;
import dev.vality.damsel.domain.*;
import dev.vality.damsel.payment_processing.*;
import dev.vality.daway.config.PostgresqlSpringBootITest;
import dev.vality.daway.dao.invoicing.iface.*;
import dev.vality.daway.domain.enums.PayerType;
import dev.vality.daway.domain.enums.PaymentChangeType;
import dev.vality.daway.domain.enums.PaymentStatus;
import dev.vality.daway.domain.enums.PaymentToolType;
import dev.vality.daway.domain.tables.pojos.Invoice;
import dev.vality.daway.domain.tables.pojos.InvoiceCart;
import dev.vality.daway.domain.tables.pojos.*;
import dev.vality.daway.service.InvoicingService;
import dev.vality.daway.utils.MockUtils;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.machinegun.msgpack.Value;
import dev.vality.sink.common.serialization.impl.PaymentEventPayloadSerializer;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static dev.vality.daway.utils.JdbcUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Slf4j
@PostgresqlSpringBootITest
public class IntegrationTest {

    @Autowired
    private InvoicingService invoicingService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private InvoiceDao invoiceDao;
    @Autowired
    private InvoiceStatusInfoDao invoiceStatusInfoDao;
    @Autowired
    private InvoiceCartDao invoiceCartDao;
    @Autowired
    private PaymentDao paymentDao;
    @Autowired
    private PaymentStatusInfoDao paymentStatusInfoDao;
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

    private final PaymentEventPayloadSerializer serializer = new PaymentEventPayloadSerializer();
    private final String invoiceId = "invoiceId";
    private final String paymentId = "paymentId";
    private final String partyId = "party_1";
    private final String shopId = "shop_id";

    @Test
    public void test() {
        cleanUpTables();

        List<MachineEvent> machineEventsFirst = getInitialInvoicePaymentEvents(invoiceId, paymentId);
        log.info("Processing first batch of machine events");
        invoicingService.handleEvents(machineEventsFirst);

        Invoice invoice = invoiceDao.get(invoiceId);
        assertEquals(partyId, invoice.getPartyId());
        assertEquals(shopId, invoice.getShopId());
        assertEquals(1, invoice.getAmount());
        assertEquals("RUB", invoice.getCurrencyCode());
        assertEquals(1, invoice.getSequenceId());
        assertEquals(0, invoice.getChangeId());

        List<InvoiceCart> invoiceCarts = invoiceCartDao.getByInvoiceId(invoiceId);
        assertEquals(1, invoiceCarts.size());
        InvoiceCart cart = invoiceCarts.get(0);
        assertEquals(1, cart.getQuantity());
        assertEquals(12, cart.getAmount());
        assertEquals("RUB", cart.getCurrencyCode());
        assertEquals("{}", cart.getMetadataJson());
        assertEquals(1, cart.getSequenceId());
        assertEquals(0, cart.getChangeId());

        InvoiceStatusInfo invoiceStatusInfo = invoiceStatusInfoDao.get(invoiceId);
        assertEquals(dev.vality.daway.domain.enums.InvoiceStatus.fulfilled, invoiceStatusInfo.getStatus());
        assertEquals("keks", invoiceStatusInfo.getDetails());
        assertEquals(1, invoiceStatusInfo.getSequenceId());
        assertEquals(1, invoiceStatusInfo.getChangeId());

        Payment payment = paymentDao.get(invoiceId, paymentId);
        assertEquals(partyId, payment.getPartyId());
        assertEquals(shopId, payment.getShopId());
        assertEquals(11, payment.getAmount());
        assertEquals("RUB", payment.getCurrencyCode());
        assertEquals(1, payment.getSequenceId());
        assertEquals(2, payment.getChangeId());

        PaymentPayerInfo paymentPayerInfo = paymentPayerInfoDao.get(invoiceId, paymentId);
        assertEquals(PaymentToolType.payment_terminal, paymentPayerInfo.getPaymentToolType());
        assertEquals(PayerType.recurrent, paymentPayerInfo.getPayerType());
        assertEquals("1", paymentPayerInfo.getRecurrentParentInvoiceId());
        assertEquals("2", paymentPayerInfo.getRecurrentParentPaymentId());
        assertEquals(1, paymentPayerInfo.getSequenceId());
        assertEquals(2, paymentPayerInfo.getChangeId());

        var paymentRoute = paymentRouteDao.get(invoiceId, paymentId);
        assertEquals(29, paymentRoute.getRouteProviderId());
        assertEquals(30, paymentRoute.getRouteTerminalId());
        assertEquals(1, paymentRoute.getSequenceId());
        assertEquals(2, paymentRoute.getChangeId());

        CashFlowLink cashFlowLink = cashFlowLinkDao.get(invoiceId, paymentId);
        assertEquals(1, cashFlowLink.getSequenceId());
        assertEquals(3, cashFlowLink.getChangeId());

        List<CashFlow> cashFlows = cashFlowDao.getByObjId(cashFlowLink.getId(), PaymentChangeType.payment);
        assertEquals(1, cashFlows.size());
        CashFlow cashFlow = cashFlows.get(0);
        assertEquals(1, cashFlow.getAmount());
        assertEquals("RUB", cashFlow.getCurrencyCode());
        assertEquals(1, cashFlow.getSourceAccountId());
        assertEquals(SystemCashFlowAccount.settlement.name(), cashFlow.getSourceAccountTypeValue());
        assertEquals(1, cashFlow.getDestinationAccountId());
        assertEquals(SystemCashFlowAccount.settlement.name(), cashFlow.getDestinationAccountTypeValue());

        PaymentFee paymentFee = paymentFeeDao.get(invoiceId, paymentId);
        assertEquals(0, paymentFee.getFee());
        assertEquals(0, paymentFee.getExternalFee());
        assertEquals(0, paymentFee.getProviderFee());
        assertEquals(0, paymentFee.getGuaranteeDeposit());
        assertEquals(1, paymentFee.getSequenceId());
        assertEquals(3, paymentFee.getChangeId());

        PaymentRiskData paymentRiskData = paymentRiskDataDao.get(invoiceId, paymentId);
        assertEquals("high", paymentRiskData.getRiskScore().name());
        assertEquals(1, paymentRiskData.getSequenceId());
        assertEquals(4, paymentRiskData.getChangeId());

        PaymentStatusInfo statusInfo = paymentStatusInfoDao.get(invoiceId, paymentId);
        assertEquals(PaymentStatus.captured, statusInfo.getStatus());
        assertEquals(1, statusInfo.getSequenceId());
        assertEquals(5, statusInfo.getChangeId());

        PaymentAdditionalInfo paymentAdditionalInfo = paymentAdditionalInfoDao.get(invoiceId, paymentId);
        assertEquals("trxId", paymentAdditionalInfo.getTransactionId());
        assertEquals("{\"lol\":\"kek\"}", paymentAdditionalInfo.getExtraJson());
        assertEquals(1, paymentAdditionalInfo.getSequenceId());
        assertEquals(6, paymentAdditionalInfo.getChangeId());

        //--- second changes - only update
        List<MachineEvent> machineEventsSecond = getInvoicePaymentChanges(invoiceId, paymentId);
        log.info("Processing second batch of machine events");
        invoicingService.handleEvents(machineEventsSecond);

        PaymentRiskData paymentRiskDataSecond = paymentRiskDataDao.get(invoiceId, paymentId);
        assertEquals("low", paymentRiskDataSecond.getRiskScore().name());
        assertEquals(2, paymentRiskDataSecond.getSequenceId());
        assertEquals(0, paymentRiskDataSecond.getChangeId());

        PaymentRecurrentInfo recurrentInfoSecond = paymentRecurrentInfoDao.get(invoiceId, paymentId);
        assertEquals("keks", recurrentInfoSecond.getToken());
        assertEquals(2, recurrentInfoSecond.getSequenceId());
        assertEquals(1, recurrentInfoSecond.getChangeId());

        var paymentRouteSecond = paymentRouteDao.get(invoiceId, paymentId);
        assertEquals(31, paymentRouteSecond.getRouteProviderId());
        assertEquals(32, paymentRouteSecond.getRouteTerminalId());
        assertEquals(2, paymentRouteSecond.getSequenceId());
        assertEquals(2, paymentRouteSecond.getChangeId());

        CashFlowLink cashFlowLinkSecond = cashFlowLinkDao.get(invoiceId, paymentId);
        assertNotEquals(cashFlowLinkSecond.getId(), cashFlowLink.getId());
        assertEquals(2, cashFlowLinkSecond.getSequenceId());
        assertEquals(3, cashFlowLinkSecond.getChangeId());

        List<CashFlow> secondCashFlows = cashFlowDao.getByObjId(cashFlowLinkSecond.getId(), PaymentChangeType.payment);
        assertEquals(1, secondCashFlows.size());
        CashFlow cashFlowSecond = secondCashFlows.get(0);
        assertEquals(1, cashFlowSecond.getAmount());
        assertEquals("RUB", cashFlowSecond.getCurrencyCode());
        assertEquals(5, cashFlowSecond.getSourceAccountId());
        assertEquals(MerchantCashFlowAccount.settlement.name(), cashFlowSecond.getSourceAccountTypeValue());
        assertEquals(1, cashFlowSecond.getDestinationAccountId());
        assertEquals(SystemCashFlowAccount.settlement.name(), cashFlowSecond.getDestinationAccountTypeValue());

        PaymentFee paymentFeeSecond = paymentFeeDao.get(invoiceId, paymentId);
        assertEquals(1, paymentFeeSecond.getFee());
        assertEquals(0, paymentFeeSecond.getExternalFee());
        assertEquals(0, paymentFeeSecond.getProviderFee());
        assertEquals(0, paymentFeeSecond.getGuaranteeDeposit());
        assertEquals(2, paymentFeeSecond.getSequenceId());
        assertEquals(3, paymentFeeSecond.getChangeId());

        //--- third changes - insert
        List<MachineEvent> machineEventsThird = getInvoicePaymentFailedChange(invoiceId, paymentId);
        log.info("Processing third batch of machine events");
        invoicingService.handleEvents(machineEventsThird);

        PaymentStatusInfo statusInfoThird = paymentStatusInfoDao.get(invoiceId, paymentId);
        assertEquals(PaymentStatus.failed, statusInfoThird.getStatus());
        assertEquals("{\"failure\":{\"operation_timeout\":{}}}", statusInfoThird.getReason());
        assertEquals(3, statusInfoThird.getSequenceId());
        assertEquals(0, statusInfoThird.getChangeId());

        //--- duplication check
        log.info("Duplication check first batch");
        invoicingService.handleEvents(machineEventsFirst);
        log.info("Duplication check second batch");
        invoicingService.handleEvents(machineEventsSecond);
        log.info("Duplication check third batch");
        invoicingService.handleEvents(machineEventsThird);
        assertDuplication();
    }

    @NotNull
    private List<MachineEvent> getInitialInvoicePaymentEvents(String invoiceId, String paymentId) {
        return List.of(
                new MachineEvent().setSourceId(invoiceId)
                        .setEventId(1)
                        .setCreatedAt(TypeUtil.temporalToString(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS)))
                        .setData(Value.bin(serializer.serialize(
                                EventPayload.invoice_changes(
                                        List.of(InvoiceChange.invoice_created(new InvoiceCreated()
                                                        .setInvoice(MockUtils.buildInvoice(invoiceId))),
                                                InvoiceChange.invoice_status_changed(new InvoiceStatusChanged()
                                                        .setStatus(
                                                                InvoiceStatus.fulfilled(
                                                                        new InvoiceFulfilled("keks")))),
                                                InvoiceChange.invoice_payment_change(new InvoicePaymentChange()
                                                        .setId(paymentId)
                                                        .setPayload(InvoicePaymentChangePayload
                                                                .invoice_payment_started(new InvoicePaymentStarted()
                                                                        .setPayment(
                                                                                MockUtils.buildPayment(paymentId))
                                                                        .setCashFlow(List.of(new FinalCashFlowPosting()
                                                                                .setSource(
                                                                                        new FinalCashFlowAccount(
                                                                                                CashFlowAccount
                                                                                                        .system(SystemCashFlowAccount.settlement),
                                                                                                1))
                                                                                .setDestination(
                                                                                        new FinalCashFlowAccount(
                                                                                                CashFlowAccount
                                                                                                        .system(SystemCashFlowAccount.settlement),
                                                                                                1))
                                                                                .setVolume(new Cash(2,
                                                                                        new CurrencyRef(
                                                                                                "RUB")))))
                                                                        .setRoute(new PaymentRoute(
                                                                                new ProviderRef(29),
                                                                                new TerminalRef(30)))
                                                                ))),
                                                InvoiceChange.invoice_payment_change(new InvoicePaymentChange()
                                                        .setId(paymentId)
                                                        .setPayload(InvoicePaymentChangePayload
                                                                .invoice_payment_cash_flow_changed(
                                                                        new InvoicePaymentCashFlowChanged(
                                                                                List.of(new FinalCashFlowPosting()
                                                                                        .setSource(
                                                                                                new FinalCashFlowAccount(
                                                                                                        CashFlowAccount
                                                                                                                .system(SystemCashFlowAccount.settlement),
                                                                                                        1))
                                                                                        .setDestination(
                                                                                                new FinalCashFlowAccount(
                                                                                                        CashFlowAccount
                                                                                                                .system(SystemCashFlowAccount.settlement),
                                                                                                        1))
                                                                                        .setVolume(new Cash(1,
                                                                                                new CurrencyRef(
                                                                                                        "RUB")))))
                                                                ))),
                                                InvoiceChange.invoice_payment_change(new InvoicePaymentChange()
                                                        .setId(paymentId)
                                                        .setPayload(InvoicePaymentChangePayload
                                                                .invoice_payment_risk_score_changed(
                                                                        new InvoicePaymentRiskScoreChanged(
                                                                                RiskScore.high)))),
                                                InvoiceChange.invoice_payment_change(new InvoicePaymentChange()
                                                        .setId(paymentId)
                                                        .setPayload(InvoicePaymentChangePayload
                                                                .invoice_payment_status_changed(
                                                                        new InvoicePaymentStatusChanged().setStatus(
                                                                                InvoicePaymentStatus.captured(
                                                                                        new InvoicePaymentCaptured()))
                                                                ))),
                                                InvoiceChange.invoice_payment_change(new InvoicePaymentChange()
                                                        .setId(paymentId)
                                                        .setPayload(InvoicePaymentChangePayload
                                                                .invoice_payment_session_change(
                                                                        new InvoicePaymentSessionChange()
                                                                                .setTarget(TargetInvoicePaymentStatus.processed(new InvoicePaymentProcessed()))
                                                                                .setPayload(SessionChangePayload.session_transaction_bound(
                                                                                        new SessionTransactionBound()
                                                                                                .setTrx(new TransactionInfo()
                                                                                                        .setId("trxId")
                                                                                                        .setExtra(Map.of("lol", "kek"))
                                                                                                ))))))
                                        ))))));
    }

    @NotNull
    private List<MachineEvent> getInvoicePaymentChanges(String invoiceId, String paymentId) {
        return List.of(
                new MachineEvent().setSourceId(invoiceId)
                        .setEventId(2)
                        .setCreatedAt(TypeUtil.temporalToString(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS)))
                        .setData(Value.bin(serializer.serialize(
                                EventPayload.invoice_changes(
                                        List.of(InvoiceChange.invoice_payment_change(new InvoicePaymentChange()
                                                        .setId(paymentId)
                                                        .setPayload(
                                                                InvoicePaymentChangePayload
                                                                        .invoice_payment_risk_score_changed(
                                                                                new InvoicePaymentRiskScoreChanged(
                                                                                        RiskScore.low)))),
                                                InvoiceChange.invoice_payment_change(new InvoicePaymentChange()
                                                        .setId(paymentId)
                                                        .setPayload(InvoicePaymentChangePayload
                                                                .invoice_payment_rec_token_acquired(
                                                                        new InvoicePaymentRecTokenAcquired("keks")
                                                                ))),
                                                InvoiceChange.invoice_payment_change(new InvoicePaymentChange()
                                                        .setId(paymentId)
                                                        .setPayload(InvoicePaymentChangePayload
                                                                .invoice_payment_route_changed(
                                                                        new InvoicePaymentRouteChanged()
                                                                                .setRoute(new PaymentRoute(
                                                                                        new ProviderRef(31),
                                                                                        new TerminalRef(32)))
                                                                ))),
                                                InvoiceChange.invoice_payment_change(new InvoicePaymentChange()
                                                        .setId(paymentId)
                                                        .setPayload(InvoicePaymentChangePayload
                                                                .invoice_payment_cash_flow_changed(
                                                                        new InvoicePaymentCashFlowChanged(
                                                                                List.of(new FinalCashFlowPosting()
                                                                                        .setSource(
                                                                                                new FinalCashFlowAccount(
                                                                                                        CashFlowAccount
                                                                                                                .merchant(MerchantCashFlowAccount.settlement),
                                                                                                        5))
                                                                                        .setDestination(
                                                                                                new FinalCashFlowAccount(
                                                                                                        CashFlowAccount
                                                                                                                .system(SystemCashFlowAccount.settlement),
                                                                                                        1))
                                                                                        .setVolume(new Cash(1,
                                                                                                new CurrencyRef(
                                                                                                        "RUB")))))
                                                                )))
                                        )))))
        );
    }

    @NotNull
    private List<MachineEvent> getInvoicePaymentFailedChange(String invoiceId, String paymentId) {
        return List.of(
                new MachineEvent().setSourceId(invoiceId)
                        .setEventId(3)
                        .setCreatedAt(TypeUtil.temporalToString(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS)))
                        .setData(Value.bin(serializer.serialize(
                                EventPayload.invoice_changes(
                                        List.of(InvoiceChange.invoice_payment_change(new InvoicePaymentChange()
                                                .setId(paymentId)
                                                .setPayload(InvoicePaymentChangePayload.invoice_payment_status_changed(
                                                        new InvoicePaymentStatusChanged()
                                                                .setStatus(InvoicePaymentStatus.failed(
                                                                        new InvoicePaymentFailed(OperationFailure
                                                                                .operation_timeout(
                                                                                        new OperationTimeout()))))
                                                ))))))))
        );
    }

    private void cleanUpTables() {
        jdbcTemplate.execute("truncate table dw.invoice cascade");
        jdbcTemplate.execute("truncate table dw.invoice_status_info cascade");
        jdbcTemplate.execute("truncate table dw.invoice_cart cascade");
        jdbcTemplate.execute("truncate table dw.payment cascade");
        jdbcTemplate.execute("truncate table dw.payment_status_info cascade");
        jdbcTemplate.execute("truncate table dw.payment_payer_info cascade");
        jdbcTemplate.execute("truncate table dw.payment_additional_info cascade");
        jdbcTemplate.execute("truncate table dw.payment_recurrent_info cascade");
        jdbcTemplate.execute("truncate table dw.payment_risk_data cascade");
        jdbcTemplate.execute("truncate table dw.payment_fee cascade");
        jdbcTemplate.execute("truncate table dw.payment_route cascade");
        jdbcTemplate.execute("truncate table dw.cash_flow_link cascade");
        jdbcTemplate.execute("truncate table dw.cash_flow cascade");
    }

    private void assertDuplication() {
        assertEquals(1, countPaymentEntity(jdbcTemplate, "payment", invoiceId, paymentId, false));
        assertEquals(3, countPaymentEntity(jdbcTemplate, "payment_status_info", invoiceId, paymentId, false));
        assertEquals(1, countPaymentEntity(jdbcTemplate, "payment_status_info", invoiceId, paymentId, true));
        assertEquals(1, countPaymentEntity(jdbcTemplate, "payment_payer_info", invoiceId, paymentId, false));
        assertEquals(1, countPaymentEntity(jdbcTemplate, "payment_additional_info", invoiceId, paymentId, false));
        assertEquals(1, countPaymentEntity(jdbcTemplate, "payment_additional_info", invoiceId, paymentId, true));
        assertEquals(1, countPaymentEntity(jdbcTemplate, "payment_recurrent_info", invoiceId, paymentId, false));
        assertEquals(1, countPaymentEntity(jdbcTemplate, "payment_recurrent_info", invoiceId, paymentId, true));
        assertEquals(2, countPaymentEntity(jdbcTemplate, "payment_risk_data", invoiceId, paymentId, false));
        assertEquals(1, countPaymentEntity(jdbcTemplate, "payment_risk_data", invoiceId, paymentId, true));
        assertEquals(3, countPaymentEntity(jdbcTemplate, "payment_fee", invoiceId, paymentId, false));
        assertEquals(1, countPaymentEntity(jdbcTemplate, "payment_fee", invoiceId, paymentId, true));
        assertEquals(2, countPaymentEntity(jdbcTemplate, "payment_route", invoiceId, paymentId, false));
        assertEquals(1, countPaymentEntity(jdbcTemplate, "payment_route", invoiceId, paymentId, true));
        assertEquals(3, countPaymentEntity(jdbcTemplate, "cash_flow_link", invoiceId, paymentId, false));
        assertEquals(1, countPaymentEntity(jdbcTemplate, "cash_flow_link", invoiceId, paymentId, true));
        assertEquals(3, countEntities(jdbcTemplate, "cash_flow"));

        assertEquals(2, countInvoiceEntity(jdbcTemplate, "invoice_status_info", invoiceId, false));
        assertEquals(1, countInvoiceEntity(jdbcTemplate, "invoice_status_info", invoiceId, true));
        assertEquals(1, countInvoiceEntity(jdbcTemplate, "invoice_cart", invoiceId, false));
    }
}
