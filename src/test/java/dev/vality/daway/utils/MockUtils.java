package dev.vality.daway.utils;

import dev.vality.damsel.base.Content;
import dev.vality.damsel.domain.*;
import dev.vality.daway.domain.enums.PaymentChangeType;
import dev.vality.daway.domain.tables.pojos.CashFlow;
import dev.vality.daway.domain.tables.pojos.CashFlowLink;
import dev.vality.daway.model.CashFlowWrapper;
import dev.vality.geck.common.util.TypeUtil;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MockUtils {

    public static Invoice buildInvoice(String invoiceId) {
        return new Invoice()
                .setId(invoiceId)
                .setOwnerId("party_1")
                .setShopId("shop_id")
                .setCreatedAt(TypeUtil.temporalToString(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS)))
                .setStatus(InvoiceStatus.unpaid(new InvoiceUnpaid()))
                .setDetails(new InvoiceDetails()
                        .setProduct("prod")
                        .setCart(new InvoiceCart(
                                List.of(new InvoiceLine()
                                        .setQuantity(1)
                                        .setProduct("product")
                                        .setPrice(new Cash(12, new CurrencyRef("RUB")))
                                        .setMetadata(new HashMap<>())))))
                .setDue(TypeUtil.temporalToString(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS)))
                .setCost(new Cash().setAmount(1).setCurrency(new CurrencyRef("RUB")))
                .setContext(new Content("type", ByteBuffer.wrap(new byte[]{})));
    }

    public static InvoicePayment buildPayment(String paymentId) {
        return new InvoicePayment()
                .setId(paymentId)
                .setCreatedAt(TypeUtil.temporalToString(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS)))
                .setStatus(InvoicePaymentStatus.pending(new InvoicePaymentPending()))
                .setCost(new Cash(11, new CurrencyRef("RUB")))
                .setDomainRevision(1)
                .setFlow(InvoicePaymentFlow.instant(new InvoicePaymentFlowInstant()))
                .setPayer(Payer.recurrent(
                        new RecurrentPayer()
                                .setPaymentTool(PaymentTool.payment_terminal(
                                        new PaymentTerminal()
                                                .setPaymentService(new PaymentServiceRef("alipay"))))
                                .setRecurrentParent(new RecurrentParentPayment("1", "2"))
                                .setContactInfo(new ContactInfo())));
    }

    public static CashFlowWrapper buildCashFlowWrapper(String invoiceId, String paymentId, Long sequenceId, Integer changeId) {
        var link = new CashFlowLink();
        link.setInvoiceId(invoiceId);
        link.setPaymentId(paymentId);
        link.setEventCreatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
        link.setSequenceId(sequenceId);
        link.setChangeId(changeId);

        List<CashFlow> cashFlows = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            var cashFlow = new CashFlow();
            cashFlow.setAmount((long) i);
            cashFlow.setObjType(PaymentChangeType.payment);
            cashFlow.setCurrencyCode("RUB");
            cashFlow.setDetails("details " + i);
            cashFlow.setSourceAccountId((long) i);
            cashFlow.setSourceAccountType(dev.vality.daway.domain.enums.CashFlowAccount.merchant);
            cashFlow.setSourceAccountTypeValue("source_account_type_value");
            cashFlow.setDestinationAccountId((long) i + sequenceId);
            cashFlow.setDestinationAccountType(dev.vality.daway.domain.enums.CashFlowAccount.external);
            cashFlow.setDestinationAccountTypeValue("destination_account_type_value");
            cashFlows.add(cashFlow);
        }

        return new CashFlowWrapper(link, cashFlows);
    }
}
