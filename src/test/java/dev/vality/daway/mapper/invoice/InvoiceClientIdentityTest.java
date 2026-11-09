package dev.vality.daway.mapper.invoice;

import com.github.benmanes.caffeine.cache.Caffeine;
import dev.vality.damsel.domain.ClientIdentity;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoiceCreated;
import dev.vality.damsel.payment_processing.InvoicePaymentChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChangePayload;
import dev.vality.damsel.payment_processing.InvoicePaymentStarted;
import dev.vality.daway.TestData;
import dev.vality.daway.dao.invoicing.iface.InvoiceDao;
import dev.vality.daway.mapper.payment.InvoicePaymentCreatedMapper;
import dev.vality.daway.model.InvoiceWrapper;
import dev.vality.daway.model.InvoiceContext;
import dev.vality.daway.service.InvoiceContextCacheService;
import dev.vality.daway.utils.MockUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class InvoiceClientIdentityTest {

    @Test
    void propagatesIdentityFromInvoiceThroughCacheAndDatabaseToPayment() {
        var invoice = MockUtils.buildInvoice("invoice");
        invoice.setClientIdentity(new ClientIdentity("customer").setUserId("user"));
        var wrapper = mapInvoice(invoice);
        assertEquals("customer", wrapper.getInvoice().getCustomerId());
        assertEquals("user", wrapper.getInvoice().getUserId());

        var dao = mock(InvoiceDao.class);
        var cache = Caffeine.newBuilder().<String, InvoiceContext>build();
        var service = new InvoiceContextCacheService(dao, cache);
        service.put(wrapper);
        assertPaymentIdentity(service, "customer", "user", "other-customer");
        verifyNoInteractions(dao);

        cache.invalidateAll();
        when(dao.get("invoice")).thenReturn(wrapper.getInvoice());
        assertPaymentIdentity(service, "customer", "user", "other-customer");
        assertPaymentIdentity(service, "customer", "user", null);
    }

    @Test
    void supportsEventsWithoutIdentityAndLegacyPaymentCustomer() {
        var wrapper = mapInvoice(MockUtils.buildInvoice("invoice"));
        assertNull(wrapper.getInvoice().getCustomerId());
        assertNull(wrapper.getInvoice().getUserId());
        var service = new InvoiceContextCacheService(mock(InvoiceDao.class), Caffeine.newBuilder().build());
        service.put(wrapper);
        assertPaymentIdentity(service, null, null, null);
        assertPaymentIdentity(service, "legacy-customer", null, "legacy-customer");
    }

    @Test
    void supportsIdentityWithoutUserId() {
        var invoice = MockUtils.buildInvoice("invoice");
        invoice.setClientIdentity(new ClientIdentity("customer"));
        var wrapper = mapInvoice(invoice);
        var service = new InvoiceContextCacheService(mock(InvoiceDao.class), Caffeine.newBuilder().build());
        service.put(wrapper);
        assertPaymentIdentity(service, "customer", null, null);
    }

    private InvoiceWrapper mapInvoice(dev.vality.damsel.domain.Invoice invoice) {
        return new InvoiceCreatedMapper().map(
                InvoiceChange.invoice_created(new InvoiceCreated(invoice)),
                TestData.createInvoiceEvent("invoice", 1L, LocalDateTime.now()), 0);
    }

    private void assertPaymentIdentity(InvoiceContextCacheService service, String customerId, String userId,
                                       String paymentCustomerId) {
        var payment = MockUtils.buildPayment("payment").setCustomerId(paymentCustomerId);
        var change = InvoiceChange.invoice_payment_change(new InvoicePaymentChange()
                .setId("payment")
                .setPayload(InvoicePaymentChangePayload.invoice_payment_started(
                        new InvoicePaymentStarted().setPayment(payment))));
        var result = new InvoicePaymentCreatedMapper(service).map(
                change, TestData.createInvoiceEvent("invoice", 2L, LocalDateTime.now()), 0).getPaymentPayerInfo();
        assertEquals(customerId, result.getCustomerId());
        assertEquals(userId, result.getUserId());
    }
}
