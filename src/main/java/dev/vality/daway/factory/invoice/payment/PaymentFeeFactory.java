package dev.vality.daway.factory.invoice.payment;

import dev.vality.damsel.domain.FinalCashFlowPosting;
import dev.vality.daway.domain.tables.pojos.PaymentFee;
import dev.vality.daway.model.CashFlowType;
import dev.vality.daway.util.CashFlowUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentFeeFactory {

    public static PaymentFee build(List<FinalCashFlowPosting> finalCashFlow,
                                   String invoiceId,
                                   String paymentId,
                                   LocalDateTime eventCreatedAt,
                                   Integer changeId,
                                   Long sequenceId) {
        PaymentFee paymentFee = new PaymentFee();
        paymentFee.setEventCreatedAt(eventCreatedAt);
        paymentFee.setInvoiceId(invoiceId);
        paymentFee.setPaymentId(paymentId);
        Map<CashFlowType, Long> parsedCashFlow = CashFlowUtil.parseCashFlow(finalCashFlow);
        paymentFee.setFee(parsedCashFlow.getOrDefault(CashFlowType.FEE, 0L));
        paymentFee.setProviderFee(parsedCashFlow.getOrDefault(CashFlowType.PROVIDER_FEE, 0L));
        paymentFee.setExternalFee(parsedCashFlow.getOrDefault(CashFlowType.EXTERNAL_FEE, 0L));
        paymentFee.setGuaranteeDeposit(parsedCashFlow.getOrDefault(CashFlowType.GUARANTEE_DEPOSIT, 0L));
        paymentFee.setSequenceId(sequenceId);
        paymentFee.setChangeId(changeId);
        return paymentFee;
    }

}
