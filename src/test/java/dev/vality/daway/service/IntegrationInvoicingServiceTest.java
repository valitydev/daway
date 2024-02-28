package dev.vality.daway.service;

import dev.vality.damsel.user_interaction.UserInteraction;
import dev.vality.daway.TestData;
import dev.vality.daway.config.PostgresqlSpringBootITest;
import dev.vality.daway.dao.invoicing.iface.PaymentSessionInfoDao;
import dev.vality.daway.domain.tables.pojos.PaymentSessionInfo;
import dev.vality.machinegun.eventsink.MachineEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@PostgresqlSpringBootITest
class IntegrationInvoicingServiceTest {

    @Autowired
    private InvoicingService invoicingService;

    @Autowired
    private PaymentSessionInfoDao paymentSessionInfoDao;


    @Test
    void invoicePaymentSessionInteractionChange() {
        UserInteraction userInteraction = TestData.userInteraction();
        var invoicePaymentSessionInteractionChange =
                TestData.createInvoicePaymentSessionInteractionChange(userInteraction);
        MachineEvent message = TestData.createInvoice(invoicePaymentSessionInteractionChange);

        invoicingService.handleEvents(List.of(message));

        PaymentSessionInfo paymentSessionInfo = paymentSessionInfoDao.get(message.getSourceId(), "test");
        assertNotNull(paymentSessionInfo);
        assertTrue(paymentSessionInfo.getUserInteraction());
        assertEquals(userInteraction.getRedirect().getGetRequest().getUri(),
                paymentSessionInfo.getUserInteractionUrl());


    }
}
