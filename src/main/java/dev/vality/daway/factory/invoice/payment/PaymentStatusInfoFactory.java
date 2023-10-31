package dev.vality.daway.factory.invoice.payment;

import dev.vality.damsel.domain.InvoicePaymentCaptured;
import dev.vality.damsel.domain.InvoicePaymentStatus;
import dev.vality.daway.domain.enums.PaymentStatus;
import dev.vality.daway.domain.tables.pojos.PaymentStatusInfo;
import dev.vality.daway.util.JsonUtil;
import dev.vality.geck.common.util.TBaseUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentStatusInfoFactory {

    public static PaymentStatusInfo build(InvoicePaymentStatus status,
                                          String invoiceId,
                                          String paymentId,
                                          LocalDateTime eventCreatedAt,
                                          Integer changeId,
                                          Long sequenceId) {
        PaymentStatusInfo statusInfo = new PaymentStatusInfo();
        statusInfo.setInvoiceId(invoiceId);
        statusInfo.setPaymentId(paymentId);
        statusInfo.setStatus(TBaseUtil.unionFieldToEnum(status, PaymentStatus.class));
        statusInfo.setEventCreatedAt(eventCreatedAt);
        if (status.isSetCancelled()) {
            statusInfo.setReason(status.getCancelled().getReason());
        } else if (status.isSetFailed()) {
            statusInfo.setReason(JsonUtil.thriftBaseToJsonString(status.getFailed()));
        } else if (status.isSetCaptured()) {
            InvoicePaymentCaptured invoicePaymentCaptured = status.getCaptured();
            statusInfo.setReason(invoicePaymentCaptured.getReason());
            if (invoicePaymentCaptured.isSetCost()) {
                statusInfo.setAmount(invoicePaymentCaptured.getCost().getAmount());
                statusInfo.setCurrencyCode(invoicePaymentCaptured.getCost().getCurrency().getSymbolicCode());
            }
        }
        statusInfo.setChangeId(changeId);
        statusInfo.setSequenceId(sequenceId);
        return statusInfo;
    }

}
