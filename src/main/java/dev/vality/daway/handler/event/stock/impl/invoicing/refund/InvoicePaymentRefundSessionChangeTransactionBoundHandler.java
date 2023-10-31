package dev.vality.daway.handler.event.stock.impl.invoicing.refund;

import dev.vality.damsel.domain.TransactionInfo;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChange;
import dev.vality.damsel.payment_processing.InvoicePaymentRefundChange;
import dev.vality.damsel.payment_processing.InvoicePaymentSessionChange;
import dev.vality.daway.dao.invoicing.iface.RefundDao;
import dev.vality.daway.domain.enums.PaymentChangeType;
import dev.vality.daway.domain.tables.pojos.Refund;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.daway.handler.event.stock.impl.invoicing.InvoicingHandler;
import dev.vality.daway.service.CashFlowService;
import dev.vality.daway.util.JsonUtil;
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
public class InvoicePaymentRefundSessionChangeTransactionBoundHandler implements InvoicingHandler {

    private final RefundDao refundDao;
    private final CashFlowService cashFlowService;
    private final MachineEventCopyFactory<Refund, Integer> machineEventCopyFactory;

    @Getter
    private Filter filter = new PathConditionFilter(new PathConditionRule(
            "invoice_payment_change" +
                    ".payload.invoice_payment_refund_change.payload.invoice_payment_session_change" +
                    ".payload.session_transaction_bound",
            new IsNullCondition().not()));

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(InvoiceChange change, MachineEvent event, Integer changeId) {
        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();
        String invoiceId = event.getSourceId();
        String paymentId = invoicePaymentChange.getId();
        InvoicePaymentRefundChange invoicePaymentRefundChange =
                invoicePaymentChange.getPayload().getInvoicePaymentRefundChange();
        String refundId = invoicePaymentRefundChange.getId();
        long sequenceId = event.getEventId();

        log.info("Start handling refund session change transaction info, " +
                        "sequenceId='{}', invoiceId='{}', paymentId='{}', refundId='{}'",
                sequenceId, invoiceId, paymentId, refundId);
        Refund refundOld = refundDao.get(invoiceId, paymentId, refundId);
        Refund refundNew = machineEventCopyFactory.create(event, sequenceId, changeId, refundOld, null);

        InvoicePaymentSessionChange sessionChange =
                invoicePaymentRefundChange.getPayload().getInvoicePaymentSessionChange();
        dev.vality.damsel.payment_processing.SessionChangePayload payload = sessionChange.getPayload();
        TransactionInfo transactionInfo = payload.getSessionTransactionBound().getTrx();
        refundNew.setSessionPayloadTransactionBoundTrxId(transactionInfo.getId());
        refundNew
                .setSessionPayloadTransactionBoundTrxExtraJson(JsonUtil.objectToJsonString(transactionInfo.getExtra()));

        refundDao.save(refundNew).ifPresentOrElse(
                id -> {
                    Long oldId = refundOld.getId();
                    refundDao.updateNotCurrent(oldId);
                    cashFlowService.save(oldId, id, PaymentChangeType.refund);
                    log.info("Refund session transaction info has been saved, " +
                                    "sequenceId='{}', invoiceId='{}', paymentId='{}', refundId='{}'",
                            sequenceId, invoiceId, paymentId, refundId);
                },
                () -> log.info("Refund session transaction info bound duplicated, " +
                                "sequenceId={}, invoiceId={}, paymentId={}, refundId={}",
                        sequenceId, invoiceId, paymentId, refundId));
    }
}
