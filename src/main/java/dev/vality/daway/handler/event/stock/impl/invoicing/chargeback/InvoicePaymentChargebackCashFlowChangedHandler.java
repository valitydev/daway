package dev.vality.daway.handler.event.stock.impl.invoicing.chargeback;

import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChargebackCashFlowChanged;
import dev.vality.damsel.payment_processing.InvoicePaymentChargebackChange;
import dev.vality.daway.dao.invoicing.iface.CashFlowDao;
import dev.vality.daway.dao.invoicing.iface.ChargebackDao;
import dev.vality.daway.domain.enums.PaymentChangeType;
import dev.vality.daway.domain.tables.pojos.CashFlow;
import dev.vality.daway.domain.tables.pojos.Chargeback;
import dev.vality.daway.factory.cash.flow.CashFlowFactory;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.daway.handler.event.stock.impl.invoicing.InvoicingHandler;
import dev.vality.daway.service.CashFlowService;
import dev.vality.geck.filter.Filter;
import dev.vality.geck.filter.PathConditionFilter;
import dev.vality.geck.filter.condition.IsNullCondition;
import dev.vality.geck.filter.rule.PathConditionRule;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoicePaymentChargebackCashFlowChangedHandler implements InvoicingHandler {

    private final ChargebackDao chargebackDao;
    private final CashFlowService cashFlowService;
    private final CashFlowDao cashFlowDao;
    private final MachineEventCopyFactory<Chargeback, Integer> machineEventCopyFactory;

    @Getter
    private Filter filter = new PathConditionFilter(new PathConditionRule(
            "invoice_payment_change" +
                    ".payload.invoice_payment_chargeback_change.payload.invoice_payment_chargeback_cash_flow_changed",
            new IsNullCondition().not()));

    @Override
    public void handle(InvoiceChange change, MachineEvent event, Integer changeId) {
        long sequenceId = event.getEventId();
        String invoiceId = event.getSourceId();
        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();
        String paymentId = invoicePaymentChange.getId();
        InvoicePaymentChargebackChange invoicePaymentChargebackChange =
                invoicePaymentChange.getPayload().getInvoicePaymentChargebackChange();
        InvoicePaymentChargebackCashFlowChanged invoicePaymentChargebackCashFlowChanged =
                invoicePaymentChargebackChange.getPayload().getInvoicePaymentChargebackCashFlowChanged();
        String chargebackId = invoicePaymentChargebackChange.getId();

        log.info("Start chargeback cash flow changed handling, " +
                        "sequenceId={}, invoiceId={}, paymentId={}, chargebackId={}",
                sequenceId, invoiceId, paymentId, chargebackId);

        Chargeback chargebackOld = chargebackDao.get(invoiceId, paymentId, chargebackId);
        Chargeback chargebackNew = machineEventCopyFactory.create(event, sequenceId, changeId, chargebackOld, null);

        chargebackDao.save(chargebackNew).ifPresentOrElse(
                id -> {
                    Long oldId = chargebackOld.getId();
                    chargebackDao.updateNotCurrent(oldId);
                    cashFlowService.save(oldId, id, PaymentChangeType.chargeback);
                    List<CashFlow> cashFlows = CashFlowFactory.build(
                            invoicePaymentChargebackCashFlowChanged.getCashFlow(),
                            id,
                            PaymentChangeType.chargeback);
                    cashFlowDao.save(cashFlows);
                    log.info("Chargeback cash flow changed have been succeeded, " +
                                    "sequenceId={}, invoiceId={}, paymentId={}, chargebackId={}",
                            sequenceId, invoiceId, paymentId, chargebackId);
                },
                () -> log.info("Chargeback cash flow changed bound duplicated, " +
                                "sequenceId={}, invoiceId={}, paymentId={}, chargebackId={}",
                        sequenceId, invoiceId, paymentId, chargebackId));
    }
}
