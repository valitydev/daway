package dev.vality.daway.mapper.payment;

import dev.vality.damsel.base.Rational;
import dev.vality.damsel.domain.ExchangeContext;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChangePayload;
import dev.vality.damsel.payment_processing.InvoicePaymentExchangeContextChanged;
import dev.vality.daway.domain.tables.pojos.PaymentExchangeContext;
import dev.vality.daway.model.PaymentWrapper;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvoicePaymentExchangeContextChangedMapperTest {

    private final InvoicePaymentExchangeContextChangedMapper mapper =
            new InvoicePaymentExchangeContextChangedMapper();

    @Test
    void mapTest() {
        LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        MachineEvent event = new MachineEvent()
                .setSourceId("invoice_id")
                .setEventId(42L)
                .setCreatedAt(TypeUtil.temporalToString(createdAt));
        InvoiceChange change = InvoiceChange.invoice_payment_change(new InvoicePaymentChange()
                .setId("payment_id")
                .setPayload(InvoicePaymentChangePayload.invoice_payment_exchange_context_changed(
                        new InvoicePaymentExchangeContextChanged(new ExchangeContext(
                                "RUB",
                                "USD",
                                new Rational(60797502L, 1000000L))))));

        PaymentWrapper wrapper = mapper.map(change, event, 7);
        PaymentExchangeContext paymentExchangeContext = wrapper.getPaymentExchangeContext();

        assertEquals("invoice_id", wrapper.getKey().getInvoiceId());
        assertEquals("payment_id", wrapper.getKey().getPaymentId());
        assertEquals("invoice_id", paymentExchangeContext.getInvoiceId());
        assertEquals("payment_id", paymentExchangeContext.getPaymentId());
        assertEquals(createdAt, paymentExchangeContext.getEventCreatedAt());
        assertEquals(42L, paymentExchangeContext.getSequenceId());
        assertEquals(7, paymentExchangeContext.getChangeId());
        assertTrue(paymentExchangeContext.getCurrent());
        assertEquals("RUB", paymentExchangeContext.getSourceCurrencyCode());
        assertEquals("USD", paymentExchangeContext.getDestinationCurrencyCode());
        assertEquals(60797502L, paymentExchangeContext.getExchangeRateRationalP());
        assertEquals(1000000L, paymentExchangeContext.getExchangeRateRationalQ());
    }
}
