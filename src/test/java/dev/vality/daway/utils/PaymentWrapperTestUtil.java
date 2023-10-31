package dev.vality.daway.utils;

import dev.vality.daway.model.InvoicingKey;
import dev.vality.daway.model.PaymentWrapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentWrapperTestUtil {

    public static void setCurrent(PaymentWrapper wrapper, boolean current) {
        if (wrapper.getPaymentStatusInfo() != null) {
            wrapper.getPaymentStatusInfo().setCurrent(current);
        }
        if (wrapper.getPaymentAdditionalInfo() != null) {
            wrapper.getPaymentAdditionalInfo().setCurrent(current);
        }
        if (wrapper.getPaymentRecurrentInfo() != null) {
            wrapper.getPaymentRecurrentInfo().setCurrent(current);
        }
        if (wrapper.getPaymentRiskData() != null) {
            wrapper.getPaymentRiskData().setCurrent(current);
        }
        if (wrapper.getPaymentFee() != null) {
            wrapper.getPaymentFee().setCurrent(current);
        }
        if (wrapper.getPaymentRoute() != null) {
            wrapper.getPaymentRoute().setCurrent(current);
        }
        if (wrapper.getCashFlowWrapper() != null) {
            wrapper.getCashFlowWrapper().getCashFlowLink().setCurrent(current);
        }
    }

    public static void setInvoiceIdAndPaymentId(PaymentWrapper wrapper, String invoiceId, String paymentId) {
        if (wrapper.getPayment() != null) {
            wrapper.getPayment().setInvoiceId(invoiceId);
            wrapper.getPayment().setPaymentId(paymentId);
        }
        if (wrapper.getPaymentStatusInfo() != null) {
            wrapper.getPaymentStatusInfo().setInvoiceId(invoiceId);
            wrapper.getPaymentStatusInfo().setPaymentId(paymentId);
        }
        if (wrapper.getPaymentPayerInfo() != null) {
            wrapper.getPaymentPayerInfo().setInvoiceId(invoiceId);
            wrapper.getPaymentPayerInfo().setPaymentId(paymentId);
        }
        if (wrapper.getPaymentAdditionalInfo() != null) {
            wrapper.getPaymentAdditionalInfo().setInvoiceId(invoiceId);
            wrapper.getPaymentAdditionalInfo().setPaymentId(paymentId);
        }
        if (wrapper.getPaymentRecurrentInfo() != null) {
            wrapper.getPaymentRecurrentInfo().setInvoiceId(invoiceId);
            wrapper.getPaymentRecurrentInfo().setPaymentId(paymentId);
        }
        if (wrapper.getPaymentRiskData() != null) {
            wrapper.getPaymentRiskData().setInvoiceId(invoiceId);
            wrapper.getPaymentRiskData().setPaymentId(paymentId);
        }
        if (wrapper.getPaymentSessionInfo() != null) {
            wrapper.getPaymentSessionInfo().setInvoiceId(invoiceId);
            wrapper.getPaymentSessionInfo().setPaymentId(paymentId);
        }
        if (wrapper.getPaymentFee() != null) {
            wrapper.getPaymentFee().setInvoiceId(invoiceId);
            wrapper.getPaymentFee().setPaymentId(paymentId);
        }
        if (wrapper.getPaymentRoute() != null) {
            wrapper.getPaymentRoute().setInvoiceId(invoiceId);
            wrapper.getPaymentRoute().setPaymentId(paymentId);
        }
        if (wrapper.getCashFlowWrapper() != null) {
            wrapper.getCashFlowWrapper().getCashFlowLink().setInvoiceId(invoiceId);
            wrapper.getCashFlowWrapper().getCashFlowLink().setPaymentId(paymentId);
        }
        if (wrapper.getKey() != null) {
            wrapper.setKey(InvoicingKey.buildKey(invoiceId, paymentId));
        }
    }

}
