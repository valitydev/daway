package dev.vality.daway.handler.event.stock.impl.invoicing.adjustment;

import dev.vality.damsel.domain.InvoicePaymentAdjustment;
import dev.vality.damsel.domain.InvoicePaymentAdjustmentState;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoicePaymentAdjustmentChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChange;
import dev.vality.daway.dao.invoicing.iface.AdjustmentDao;
import dev.vality.daway.dao.invoicing.iface.CashFlowDao;
import dev.vality.daway.dao.invoicing.iface.PaymentDao;
import dev.vality.daway.domain.enums.AdjustmentCashFlowType;
import dev.vality.daway.domain.enums.AdjustmentStatus;
import dev.vality.daway.domain.enums.PaymentChangeType;
import dev.vality.daway.domain.enums.PaymentStatus;
import dev.vality.daway.domain.tables.pojos.Adjustment;
import dev.vality.daway.domain.tables.pojos.CashFlow;
import dev.vality.daway.domain.tables.pojos.Payment;
import dev.vality.daway.factory.cash.flow.CashFlowFactory;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.daway.handler.event.stock.impl.invoicing.InvoicingHandler;
import dev.vality.daway.util.AdjustmentUtils;
import dev.vality.geck.common.util.TBaseUtil;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.geck.filter.Filter;
import dev.vality.geck.filter.PathConditionFilter;
import dev.vality.geck.filter.condition.IsNullCondition;
import dev.vality.geck.filter.rule.PathConditionRule;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoicePaymentAdjustmentCreatedHandler implements InvoicingHandler {

    private final AdjustmentDao adjustmentDao;
    private final PaymentDao paymentDao;
    private final CashFlowDao cashFlowDao;
    private final MachineEventCopyFactory<Adjustment, Integer> machineEventCopyFactory;

    @Getter
    private Filter filter = new PathConditionFilter(new PathConditionRule(
            "invoice_payment_change" +
                    ".payload.invoice_payment_adjustment_change.payload.invoice_payment_adjustment_created",
            new IsNullCondition().not()));

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(InvoiceChange invoiceChange, MachineEvent event, Integer changeId) {
        long sequenceId = event.getEventId();
        String invoiceId = event.getSourceId();
        InvoicePaymentChange invoicePaymentChange = invoiceChange.getInvoicePaymentChange();
        String paymentId = invoicePaymentChange.getId();
        InvoicePaymentAdjustmentChange invoicePaymentAdjustmentChange = invoicePaymentChange.getPayload()
                .getInvoicePaymentAdjustmentChange();
        InvoicePaymentAdjustment invoicePaymentAdjustment = invoicePaymentAdjustmentChange
                .getPayload().getInvoicePaymentAdjustmentCreated().getAdjustment();
        String adjustmentId = invoicePaymentAdjustment.getId();

        log.info("Start adjustment created handling, sequenceId={}, invoiceId={}, paymentId={}, adjustmentId={}",
                sequenceId, invoiceId, paymentId, adjustmentId);

        Adjustment adjustment = machineEventCopyFactory.create(event, sequenceId, changeId, null);
        adjustment.setDomainRevision(invoicePaymentAdjustment.getDomainRevision());
        adjustment.setAdjustmentId(adjustmentId);
        adjustment.setPaymentId(paymentId);
        adjustment.setInvoiceId(invoiceId);
        Payment payment = paymentDao.get(invoiceId, paymentId);
        adjustment.setPartyId(payment.getPartyId());
        adjustment.setShopId(payment.getShopId());
        adjustment.setCreatedAt(TypeUtil.stringToLocalDateTime(invoicePaymentAdjustment.getCreatedAt()));
        adjustment.setStatus(TBaseUtil.unionFieldToEnum(invoicePaymentAdjustment.getStatus(), AdjustmentStatus.class));
        if (invoicePaymentAdjustment.getStatus().isSetCaptured()) {
            adjustment.setStatusCapturedAt(
                    TypeUtil.stringToLocalDateTime(invoicePaymentAdjustment.getStatus().getCaptured().getAt()));
        } else if (invoicePaymentAdjustment.getStatus().isSetCancelled()) {
            adjustment.setStatusCancelledAt(
                    TypeUtil.stringToLocalDateTime(invoicePaymentAdjustment.getStatus().getCancelled().getAt()));
        }
        adjustment.setReason(invoicePaymentAdjustment.getReason());
        if (invoicePaymentAdjustment.isSetDomainRevision()) {
            adjustment.setPartyRevision(invoicePaymentAdjustment.getDomainRevision());
        }
        if (invoicePaymentAdjustment.isSetState()) {
            InvoicePaymentAdjustmentState invoicePaymentAdjustmentState = invoicePaymentAdjustment.getState();
            if (invoicePaymentAdjustmentState.isSetCashFlow()) {
                adjustment.setDomainRevision(
                        invoicePaymentAdjustmentState.getCashFlow().getScenario().getDomainRevision());
            } else if (invoicePaymentAdjustmentState.isSetStatusChange()) {
                PaymentStatus paymentStatus = TBaseUtil.unionFieldToEnum(
                        invoicePaymentAdjustmentState.getStatusChange().getScenario().getTargetStatus(),
                        PaymentStatus.class);
                adjustment.setPaymentStatus(paymentStatus);
            }
        }

        adjustment.setAmount(AdjustmentUtils.calculateMerchantAmountDiff(invoicePaymentAdjustment));
        adjustment.setProviderAmountDiff(AdjustmentUtils.calculateProviderAmountDiff(invoicePaymentAdjustment));
        adjustment.setSystemAmountDiff(AdjustmentUtils.calculateSystemAmountDiff(invoicePaymentAdjustment));
        adjustment.setExternalIncomeAmountDiff(
                AdjustmentUtils.calculateExternalIncomeAmountDiff(invoicePaymentAdjustment));
        adjustment.setExternalOutcomeAmountDiff(
                AdjustmentUtils.calculateExternalOutcomeAmountDiff(invoicePaymentAdjustment));

        adjustmentDao.save(adjustment).ifPresentOrElse(
                id -> {
                    List<CashFlow> newCashFlowList = CashFlowFactory.build(
                            invoicePaymentAdjustment.getNewCashFlow(),
                            id,
                            PaymentChangeType.adjustment,
                            AdjustmentCashFlowType.new_cash_flow);
                    cashFlowDao.save(newCashFlowList);
                    List<CashFlow> oldCashFlowList = CashFlowFactory.build(
                            invoicePaymentAdjustment.getOldCashFlowInverse(),
                            id,
                            PaymentChangeType.adjustment,
                            AdjustmentCashFlowType.old_cash_flow_inverse);
                    cashFlowDao.save(oldCashFlowList);
                    log.info("Adjustment has been saved, sequenceId={}, invoiceId={}, paymentId={}, adjustmentId={}",
                            sequenceId, invoiceId, paymentId, adjustmentId);
                },
                () -> log
                        .info("Adjustment bound duplicated, sequenceId={}, invoiceId={}, paymentId={}, adjustmentId={}",
                                sequenceId, invoiceId, paymentId, adjustmentId));
    }

}
