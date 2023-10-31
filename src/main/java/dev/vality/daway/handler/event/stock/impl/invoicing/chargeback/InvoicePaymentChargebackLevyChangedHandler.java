package dev.vality.daway.handler.event.stock.impl.invoicing.chargeback;

import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChargebackChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChargebackLevyChanged;
import dev.vality.daway.dao.invoicing.iface.ChargebackDao;
import dev.vality.daway.domain.enums.PaymentChangeType;
import dev.vality.daway.domain.tables.pojos.Chargeback;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoicePaymentChargebackLevyChangedHandler implements InvoicingHandler {

    private final ChargebackDao chargebackDao;
    private final CashFlowService cashFlowService;
    private final MachineEventCopyFactory<Chargeback, Integer> machineEventCopyFactory;

    @Getter
    private Filter filter = new PathConditionFilter(new PathConditionRule(
            "invoice_payment_change" +
                    ".payload.invoice_payment_chargeback_change.payload.invoice_payment_chargeback_levy_changed",
            new IsNullCondition().not()));

    @Override
    public void handle(InvoiceChange change, MachineEvent event, Integer changeId) {
        long sequenceId = event.getEventId();
        String invoiceId = event.getSourceId();
        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();
        String paymentId = invoicePaymentChange.getId();
        InvoicePaymentChargebackChange invoicePaymentChargebackChange =
                invoicePaymentChange.getPayload().getInvoicePaymentChargebackChange();
        String chargebackId = invoicePaymentChargebackChange.getId();

        log.info("Start chargeback levy change handling, sequenceId={}, invoiceId={}, paymentId={}, chargebackId={}",
                sequenceId, invoiceId, paymentId, chargebackId);

        Chargeback chargebackOld = chargebackDao.get(invoiceId, paymentId, chargebackId);
        Chargeback chargebackNew = machineEventCopyFactory.create(event, sequenceId, changeId, chargebackOld, null);

        InvoicePaymentChargebackLevyChanged invoicePaymentChargebackLevyChanged =
                invoicePaymentChargebackChange.getPayload().getInvoicePaymentChargebackLevyChanged();
        chargebackNew.setLevyAmount(invoicePaymentChargebackLevyChanged.getLevy().getAmount());
        chargebackNew.setLevyCurrencyCode(
                invoicePaymentChargebackLevyChanged.getLevy().getCurrency().getSymbolicCode());

        chargebackDao.save(chargebackNew).ifPresentOrElse(
                id -> {
                    Long oldId = chargebackOld.getId();
                    chargebackDao.updateNotCurrent(oldId);
                    cashFlowService.save(oldId, id, PaymentChangeType.chargeback);
                    log.info("Chargeback levy changed have been succeeded, " +
                                    "sequenceId={}, invoiceId={}, paymentId={}, chargebackId={}",
                            sequenceId, invoiceId, paymentId, chargebackId);
                },
                () -> log.info("Chargeback levy changed bound duplicated, " +
                                "sequenceId={}, invoiceId={}, paymentId={}, chargebackId={}",
                        sequenceId, invoiceId, paymentId, chargebackId));
    }

}
