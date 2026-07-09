package dev.vality.daway.mapper.payment;

import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.daway.TestData;
import dev.vality.daway.domain.tables.pojos.PaymentExchangeContext;
import dev.vality.daway.model.PaymentWrapper;
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
        MachineEvent event = TestData.createInvoiceEvent("invoice_id", 42L, createdAt);
        InvoiceChange change = TestData.createInvoicePaymentExchangeContextChanged("payment_id");

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
