package dev.vality.daway.mapper.invoice;

import dev.vality.damsel.domain.ClientIdentity;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoiceCreated;
import dev.vality.daway.TestData;
import dev.vality.daway.model.InvoiceWrapper;
import dev.vality.daway.utils.MockUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InvoiceClientIdentityTest {

    @Test
    void savesClientIdentityInInvoice() {
        var invoice = MockUtils.buildInvoice("invoice");
        invoice.setClientIdentity(new ClientIdentity("customer").setUserId("user"));
        var result = mapInvoice(invoice).getInvoice();
        assertEquals("customer", result.getCustomerId());
        assertEquals("user", result.getUserId());
    }

    @Test
    void supportsEventsWithoutIdentity() {
        var result = mapInvoice(MockUtils.buildInvoice("invoice")).getInvoice();
        assertNull(result.getCustomerId());
        assertNull(result.getUserId());
    }

    @Test
    void supportsIdentityWithoutUserId() {
        var invoice = MockUtils.buildInvoice("invoice");
        invoice.setClientIdentity(new ClientIdentity("customer"));
        var result = mapInvoice(invoice).getInvoice();
        assertEquals("customer", result.getCustomerId());
        assertNull(result.getUserId());
    }

    private InvoiceWrapper mapInvoice(dev.vality.damsel.domain.Invoice invoice) {
        return new InvoiceCreatedMapper().map(
                InvoiceChange.invoice_created(new InvoiceCreated(invoice)),
                TestData.createInvoiceEvent("invoice", 1L, LocalDateTime.now()), 0);
    }
}
