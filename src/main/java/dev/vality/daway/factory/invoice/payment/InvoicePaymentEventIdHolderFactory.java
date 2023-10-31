package dev.vality.daway.factory.invoice.payment;

import dev.vality.daway.domain.tables.pojos.CashFlowLink;
import dev.vality.daway.model.InvoicePaymentEventIdHolder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InvoicePaymentEventIdHolderFactory {

    public static InvoicePaymentEventIdHolder build(CashFlowLink link) {
        return new InvoicePaymentEventIdHolder(
                link.getInvoiceId(),
                link.getPaymentId(),
                link.getSequenceId(),
                link.getChangeId()
        );
    }

}
