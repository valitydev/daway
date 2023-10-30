package dev.vality.daway.handler.event.stock.impl.invoicing.adjustment;

import dev.vality.damsel.domain.InvoicePaymentAdjustmentStatus;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoicePaymentAdjustmentChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChange;
import dev.vality.daway.dao.invoicing.iface.AdjustmentDao;
import dev.vality.daway.dao.invoicing.iface.CashFlowDao;
import dev.vality.daway.domain.enums.AdjustmentCashFlowType;
import dev.vality.daway.domain.enums.AdjustmentStatus;
import dev.vality.daway.domain.tables.pojos.Adjustment;
import dev.vality.daway.domain.tables.pojos.CashFlow;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.daway.handler.event.stock.impl.invoicing.InvoicingHandler;
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
public class InvoicePaymentAdjustmentStatusChangedHandler implements InvoicingHandler {

    private final AdjustmentDao adjustmentDao;
    private final CashFlowDao cashFlowDao;
    private final MachineEventCopyFactory<Adjustment, Integer> machineEventCopyFactory;

    @Getter
    private Filter filter = new PathConditionFilter(new PathConditionRule(
            "invoice_payment_change" +
                    ".payload.invoice_payment_adjustment_change.payload.invoice_payment_adjustment_status_changed",
            new IsNullCondition().not()));

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(InvoiceChange invoiceChange, MachineEvent event, Integer changeId) {
        long sequenceId = event.getEventId();
        String invoiceId = event.getSourceId();
        InvoicePaymentChange invoicePaymentChange = invoiceChange.getInvoicePaymentChange();
        String paymentId = invoiceChange.getInvoicePaymentChange().getId();
        InvoicePaymentAdjustmentChange invoicePaymentAdjustmentChange =
                invoicePaymentChange.getPayload().getInvoicePaymentAdjustmentChange();
        InvoicePaymentAdjustmentStatus invoicePaymentAdjustmentStatus =
                invoicePaymentAdjustmentChange.getPayload().getInvoicePaymentAdjustmentStatusChanged().getStatus();
        String adjustmentId = invoicePaymentAdjustmentChange.getId();

        log.info("Start adjustment status changed handling, " +
                        "sequenceId={}, invoiceId={}, paymentId={}, adjustmentId={}, status={}",
                sequenceId, invoiceId, paymentId, adjustmentId,
                invoicePaymentAdjustmentStatus.getSetField().getFieldName());
        Adjustment adjustmentOld = adjustmentDao.get(invoiceId, paymentId, adjustmentId);
        Adjustment adjustmentNew = machineEventCopyFactory.create(event, sequenceId, changeId, adjustmentOld, null);

        adjustmentNew.setStatus(TBaseUtil.unionFieldToEnum(invoicePaymentAdjustmentStatus, AdjustmentStatus.class));
        if (invoicePaymentAdjustmentStatus.isSetCaptured()) {
            adjustmentNew.setStatusCapturedAt(
                    TypeUtil.stringToLocalDateTime(invoicePaymentAdjustmentStatus.getCaptured().getAt()));
            adjustmentNew.setStatusCancelledAt(null);
        } else if (invoicePaymentAdjustmentStatus.isSetCancelled()) {
            adjustmentNew.setStatusCapturedAt(null);
            adjustmentNew.setStatusCancelledAt(
                    TypeUtil.stringToLocalDateTime(invoicePaymentAdjustmentStatus.getCancelled().getAt()));
        }

        adjustmentDao.save(adjustmentNew).ifPresentOrElse(
                id -> {
                    Long oldId = adjustmentOld.getId();
                    adjustmentDao.updateNotCurrent(oldId);
                    List<CashFlow> newCashFlows =
                            cashFlowDao.getForAdjustments(oldId, AdjustmentCashFlowType.new_cash_flow);
                    newCashFlows.forEach(pcf -> {
                        pcf.setId(null);
                        pcf.setObjId(id);
                    });
                    cashFlowDao.save(newCashFlows);
                    List<CashFlow> oldCashFlows =
                            cashFlowDao.getForAdjustments(oldId, AdjustmentCashFlowType.old_cash_flow_inverse);
                    oldCashFlows.forEach(pcf -> {
                        pcf.setId(null);
                        pcf.setObjId(id);
                    });
                    cashFlowDao.save(oldCashFlows);
                    log.info("Adjustment status change has been saved, " +
                                    "sequenceId={}, invoiceId={}, paymentId={}, adjustmentId={}",
                            sequenceId, invoiceId, paymentId, adjustmentId);
                },
                () -> log
                        .info("Adjustment status change bound duplicated," +
                                        " sequenceId={}, invoiceId={}, paymentId={}, adjustmentId={}",
                                sequenceId, invoiceId, paymentId, adjustmentId));
    }

}
