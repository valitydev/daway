package dev.vality.daway.mapper.payment;

import dev.vality.damsel.domain.Cash;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoicePaymentCashChanged;
import dev.vality.damsel.payment_processing.InvoicePaymentChange;
import dev.vality.daway.domain.tables.pojos.PaymentCashChange;
import dev.vality.daway.mapper.Mapper;
import dev.vality.daway.model.InvoicingKey;
import dev.vality.daway.model.PaymentWrapper;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.geck.filter.Filter;
import dev.vality.geck.filter.PathConditionFilter;
import dev.vality.geck.filter.condition.IsNullCondition;
import dev.vality.geck.filter.rule.PathConditionRule;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoicePaymentCashChangedMapper implements Mapper<PaymentWrapper> {

    private Filter filter = new PathConditionFilter(new PathConditionRule(
            "invoice_payment_change.payload.invoice_payment_cash_changed",
            new IsNullCondition().not()));

    @Override
    public PaymentWrapper map(InvoiceChange change, MachineEvent event, Integer changeId) {
        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();
        String invoiceId = event.getSourceId();
        String paymentId = invoicePaymentChange.getId();
        long sequenceId = event.getEventId();
        log.info("Start mapping payment cash change, sequenceId='{}', changeId='{}', invoiceId='{}', paymentId='{}'",
                sequenceId, changeId, invoiceId, paymentId);

        InvoicePaymentCashChanged invoicePaymentCashChanged =
                invoicePaymentChange.getPayload().getInvoicePaymentCashChanged();
        PaymentWrapper paymentWrapper = new PaymentWrapper();
        paymentWrapper.setKey(InvoicingKey.buildKey(invoiceId, paymentId));

        PaymentCashChange cashChange = new PaymentCashChange();
        cashChange.setWtime(null);
        cashChange.setId(null);
        cashChange.setChangeId(changeId);
        cashChange.setSequenceId(sequenceId);
        cashChange.setPaymentId(paymentId);
        cashChange.setInvoiceId(invoiceId);
        cashChange.setCurrent(true);
        cashChange.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));

        Cash newCash = invoicePaymentCashChanged.getNewCash();
        cashChange.setNewAmount(newCash.getAmount());
        cashChange.setNewCurrencyCode(newCash.getCurrency().getSymbolicCode());
        Cash oldCash = invoicePaymentCashChanged.getOldCash();
        cashChange.setOldAmount(oldCash.getAmount());
        cashChange.setOldCurrencyCode(oldCash.getCurrency().getSymbolicCode());

        paymentWrapper.setPaymentCashChange(cashChange);
        log.info("Payment cash has been mapped, sequenceId='{}', changeId='{}', invoiceId='{}', paymentId='{}'",
                sequenceId, changeId, invoiceId, paymentId);
        return paymentWrapper;
    }

    @Override
    public Filter<InvoiceChange> getFilter() {
        return filter;
    }

}
