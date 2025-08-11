package dev.vality.daway.handler.event.stock.impl.invoicing.refund;

import dev.vality.damsel.domain.InvoicePaymentRefund;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChange;
import dev.vality.damsel.payment_processing.InvoicePaymentRefundChange;
import dev.vality.damsel.payment_processing.InvoicePaymentRefundCreated;
import dev.vality.daway.dao.invoicing.iface.CashFlowDao;
import dev.vality.daway.dao.invoicing.iface.PaymentDao;
import dev.vality.daway.dao.invoicing.iface.RefundDao;
import dev.vality.daway.domain.enums.PaymentChangeType;
import dev.vality.daway.domain.enums.RefundStatus;
import dev.vality.daway.domain.tables.pojos.CashFlow;
import dev.vality.daway.domain.tables.pojos.Payment;
import dev.vality.daway.domain.tables.pojos.Refund;
import dev.vality.daway.factory.cash.flow.CashFlowFactory;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.daway.handler.event.stock.impl.invoicing.InvoicingHandler;
import dev.vality.daway.util.JsonUtil;
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
public class InvoicePaymentRefundCreatedHandler implements InvoicingHandler {

    private final RefundDao refundDao;
    private final PaymentDao paymentDao;
    private final CashFlowDao cashFlowDao;
    private final MachineEventCopyFactory<Refund, Integer> machineEventCopyFactory;

    @Getter
    private Filter filter = new PathConditionFilter(new PathConditionRule(
            "invoice_payment_change.payload.invoice_payment_refund_change.payload.invoice_payment_refund_created",
            new IsNullCondition().not()));

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(InvoiceChange invoiceChange, MachineEvent event, Integer changeId) {
        long sequenceId = event.getEventId();
        String invoiceId = event.getSourceId();
        InvoicePaymentChange invoicePaymentChange = invoiceChange.getInvoicePaymentChange();
        String paymentId = invoicePaymentChange.getId();

        InvoicePaymentRefundChange invoicePaymentRefundChange = invoicePaymentChange.getPayload()
                .getInvoicePaymentRefundChange();
        InvoicePaymentRefundCreated invoicePaymentRefundCreated = invoicePaymentRefundChange.getPayload()
                .getInvoicePaymentRefundCreated();

        InvoicePaymentRefund invoicePaymentRefund = invoicePaymentRefundCreated.getRefund();

        String refundId = invoicePaymentRefund.getId();
        log.info("Start refund created handling, sequenceId={}, invoiceId={}, paymentId={}, refundId={}",
                sequenceId, invoiceId, paymentId, refundId);

        Refund refund = machineEventCopyFactory.create(event, sequenceId, changeId, null);

        refund.setDomainRevision(invoicePaymentRefund.getDomainRevision());
        refund.setRefundId(refundId);
        refund.setPaymentId(paymentId);
        refund.setInvoiceId(invoiceId);
        refund.setExternalId(invoicePaymentRefund.getExternalId());

        Payment payment = paymentDao.get(invoiceId, paymentId);
        refund.setPartyId(payment.getPartyId());
        refund.setShopId(payment.getShopId());
        refund.setCreatedAt(TypeUtil.stringToLocalDateTime(invoicePaymentRefund.getCreatedAt()));
        refund.setStatus(TBaseUtil.unionFieldToEnum(invoicePaymentRefund.getStatus(), RefundStatus.class));
        if (invoicePaymentRefund.getStatus().isSetFailed()) {
            refund.setStatusFailedFailure(
                    JsonUtil.thriftBaseToJsonString(invoicePaymentRefund.getStatus().getFailed()));
        }

        if (invoicePaymentRefund.isSetCash()) {
            refund.setAmount(invoicePaymentRefund.getCash().getAmount());
            refund.setCurrencyCode(invoicePaymentRefund.getCash().getCurrency().getSymbolicCode());
        } else {
            refund.setAmount(payment.getAmount());
            refund.setCurrencyCode(payment.getCurrencyCode());
        }
        refund.setReason(invoicePaymentRefund.getReason());
        if (invoicePaymentRefund.isSetDomainRevision()) {
            refund.setPartyRevision(invoicePaymentRefund.getDomainRevision());
        }

        refundDao.save(refund).ifPresentOrElse(
                id -> {
                    List<CashFlow> cashFlowList = CashFlowFactory.build(
                            invoicePaymentRefundCreated.getCashFlow(), id, PaymentChangeType.refund);
                    cashFlowDao.save(cashFlowList);
                    refundDao.updateCommissions(id);
                    log.info("Refund has been saved, sequenceId={}, invoiceId={}, paymentId={}, refundId={}",
                            sequenceId, invoiceId, paymentId, refundId);
                },
                () -> log.info("Refund bound duplicated, sequenceId={}, invoiceId={}, paymentId={}, refundId={}",
                        sequenceId, invoiceId, paymentId, refundId));
    }

}
