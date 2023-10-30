package dev.vality.daway.util;

import dev.vality.damsel.domain.FinalCashFlowPosting;
import dev.vality.damsel.domain.InvoicePaymentAdjustment;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AdjustmentUtils {

    public static long calculateMerchantAmountDiff(InvoicePaymentAdjustment invoicePaymentAdjustment) {
        return calculateAmountDiff(invoicePaymentAdjustment, CashFlowUtil::computeMerchantAmount);
    }

    public static long calculateProviderAmountDiff(InvoicePaymentAdjustment invoicePaymentAdjustment) {
        return calculateAmountDiff(invoicePaymentAdjustment, CashFlowUtil::computeProviderAmount);
    }

    public static long calculateSystemAmountDiff(InvoicePaymentAdjustment invoicePaymentAdjustment) {
        return calculateAmountDiff(invoicePaymentAdjustment, CashFlowUtil::computeSystemAmount);
    }

    public static long calculateExternalIncomeAmountDiff(InvoicePaymentAdjustment invoicePaymentAdjustment) {
        return calculateAmountDiff(invoicePaymentAdjustment, CashFlowUtil::computeExternalIncomeAmount);
    }

    public static long calculateExternalOutcomeAmountDiff(InvoicePaymentAdjustment invoicePaymentAdjustment) {
        return calculateAmountDiff(invoicePaymentAdjustment, CashFlowUtil::computeExternalOutcomeAmount);
    }

    private static long calculateAmountDiff(InvoicePaymentAdjustment invoicePaymentAdjustment,
                                            Function<List<FinalCashFlowPosting>, Long> calculateAmountDiff) {
        Long oldAmount = calculateAmountDiff.apply(invoicePaymentAdjustment.getOldCashFlowInverse());
        Long newAmount = calculateAmountDiff.apply(invoicePaymentAdjustment.getNewCashFlow());
        return newAmount + oldAmount;
    }

}
