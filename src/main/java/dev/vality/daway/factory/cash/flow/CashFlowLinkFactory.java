package dev.vality.daway.factory.cash.flow;

import dev.vality.daway.domain.tables.pojos.CashFlowLink;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CashFlowLinkFactory {

    public static CashFlowLink build(String paymentId,
                                     String invoiceId,
                                     LocalDateTime eventCreatedAt,
                                     Integer changeId,
                                     Long sequenceId) {
        CashFlowLink link = new CashFlowLink();
        link.setPaymentId(paymentId);
        link.setInvoiceId(invoiceId);
        link.setEventCreatedAt(eventCreatedAt);
        link.setChangeId(changeId);
        link.setSequenceId(sequenceId);
        return link;
    }

}
