package dev.vality.daway.mapper.payment;

import dev.vality.damsel.base.Rational;
import dev.vality.damsel.domain.ExchangeContext;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChange;
import dev.vality.damsel.payment_processing.InvoicePaymentExchangeContextChanged;
import dev.vality.daway.domain.tables.pojos.PaymentExchangeContext;
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
public class InvoicePaymentExchangeContextChangedMapper implements Mapper<PaymentWrapper> {

    private Filter filter = new PathConditionFilter(new PathConditionRule(
            "invoice_payment_change.payload.invoice_payment_exchange_context_changed",
            new IsNullCondition().not()));

    @Override
    public PaymentWrapper map(InvoiceChange change, MachineEvent event, Integer changeId) {
        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();
        String invoiceId = event.getSourceId();
        String paymentId = invoicePaymentChange.getId();
        long sequenceId = event.getEventId();
        log.info("Start mapping payment exchange context change, sequenceId='{}', changeId='{}', invoiceId='{}', "
                        + "paymentId='{}'",
                sequenceId, changeId, invoiceId, paymentId);

        InvoicePaymentExchangeContextChanged exchangeContextChanged =
                invoicePaymentChange.getPayload().getInvoicePaymentExchangeContextChanged();
        ExchangeContext exchangeContext = exchangeContextChanged.getExchangeContext();
        Rational exchangeRate = exchangeContext.getExchangeRate();

        PaymentExchangeContext paymentExchangeContext = new PaymentExchangeContext();
        paymentExchangeContext.setWtime(null);
        paymentExchangeContext.setId(null);
        paymentExchangeContext.setChangeId(changeId);
        paymentExchangeContext.setSequenceId(sequenceId);
        paymentExchangeContext.setPaymentId(paymentId);
        paymentExchangeContext.setInvoiceId(invoiceId);
        paymentExchangeContext.setCurrent(true);
        paymentExchangeContext.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        paymentExchangeContext.setSourceCurrencyCode(exchangeContext.getSourceCurrency());
        paymentExchangeContext.setDestinationCurrencyCode(exchangeContext.getDestinationCurrency());
        paymentExchangeContext.setExchangeRateRationalP(exchangeRate.getP());
        paymentExchangeContext.setExchangeRateRationalQ(exchangeRate.getQ());

        PaymentWrapper paymentWrapper = new PaymentWrapper();
        paymentWrapper.setKey(InvoicingKey.buildKey(invoiceId, paymentId));
        paymentWrapper.setPaymentExchangeContext(paymentExchangeContext);
        log.info("Payment exchange context has been mapped, sequenceId='{}', changeId='{}', invoiceId='{}', "
                        + "paymentId='{}'",
                sequenceId, changeId, invoiceId, paymentId);
        return paymentWrapper;
    }

    @Override
    public Filter<InvoiceChange> getFilter() {
        return filter;
    }
}
