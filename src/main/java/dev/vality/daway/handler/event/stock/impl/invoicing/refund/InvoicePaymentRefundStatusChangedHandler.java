package dev.vality.daway.handler.event.stock.impl.invoicing.refund;

import dev.vality.damsel.domain.InvoicePaymentRefundStatus;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChange;
import dev.vality.damsel.payment_processing.InvoicePaymentRefundChange;
import dev.vality.daway.dao.invoicing.iface.RefundDao;
import dev.vality.daway.domain.enums.PaymentChangeType;
import dev.vality.daway.domain.enums.RefundStatus;
import dev.vality.daway.domain.tables.pojos.Refund;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.daway.handler.event.stock.impl.invoicing.InvoicingHandler;
import dev.vality.daway.service.CashFlowService;
import dev.vality.daway.util.JsonUtil;
import dev.vality.geck.common.util.TBaseUtil;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoicePaymentRefundStatusChangedHandler implements InvoicingHandler {

    private final RefundDao refundDao;
    private final CashFlowService cashFlowService;
    private final MachineEventCopyFactory<Refund, Integer> machineEventCopyFactory;

    @Getter
    private Filter filter = new PathConditionFilter(new PathConditionRule(
            "invoice_payment_change" +
                    ".payload.invoice_payment_refund_change.payload.invoice_payment_refund_status_changed",
            new IsNullCondition().not()));

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(InvoiceChange invoiceChange, MachineEvent event, Integer changeId) {
        long sequenceId = event.getEventId();
        String invoiceId = event.getSourceId();
        InvoicePaymentChange invoicePaymentChange = invoiceChange.getInvoicePaymentChange();
        String paymentId = invoiceChange.getInvoicePaymentChange().getId();
        InvoicePaymentRefundChange invoicePaymentRefundChange = invoicePaymentChange.getPayload()
                .getInvoicePaymentRefundChange();
        InvoicePaymentRefundStatus invoicePaymentRefundStatus =
                invoicePaymentRefundChange.getPayload().getInvoicePaymentRefundStatusChanged().getStatus();
        String refundId = invoicePaymentRefundChange.getId();

        log.info("Start refund status changed handling, " +
                        "sequenceId={}, invoiceId={}, paymentId={}, refundId={}, status={}",
                sequenceId, invoiceId, paymentId, refundId, invoicePaymentRefundStatus.getSetField().getFieldName());

        Refund refundOld = refundDao.get(invoiceId, paymentId, refundId);
        Refund refundNew = machineEventCopyFactory.create(event, sequenceId, changeId, refundOld, null);

        refundNew.setStatus(TBaseUtil.unionFieldToEnum(invoicePaymentRefundStatus, RefundStatus.class));
        if (invoicePaymentRefundStatus.isSetFailed()) {
            refundNew.setStatusFailedFailure(JsonUtil.thriftBaseToJsonString(invoicePaymentRefundStatus.getFailed()));
        } else {
            refundNew.setStatusFailedFailure(null);
        }

        refundDao.save(refundNew).ifPresentOrElse(
                id -> {
                    Long oldId = refundOld.getId();
                    refundDao.updateNotCurrent(oldId);
                    cashFlowService.save(oldId, id, PaymentChangeType.refund);
                    log.info("Refund status changed has been saved, " +
                                    "sequenceId='{}', invoiceId='{}', paymentId='{}', refundId='{}'",
                            sequenceId, invoiceId, paymentId, refundId);
                },
                () -> log.info("Refund status changed bound duplicated, " +
                                "sequenceId={}, invoiceId={}, paymentId={}, refundId={}",
                        sequenceId, invoiceId, paymentId, refundId));
    }

}
