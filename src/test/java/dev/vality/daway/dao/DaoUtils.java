package dev.vality.daway.dao;

import dev.vality.damsel.domain.ExternalCashFlowAccount;
import dev.vality.damsel.domain.MerchantCashFlowAccount;
import dev.vality.damsel.domain.ProviderCashFlowAccount;
import dev.vality.damsel.domain.SystemCashFlowAccount;
import dev.vality.daway.domain.enums.CashFlowAccount;
import dev.vality.daway.domain.enums.PaymentChangeType;
import dev.vality.daway.domain.tables.pojos.CashFlow;

public class DaoUtils {

    public static CashFlow createCashFlow(long objId, long amount, String currencyCode, long sourceAccountId,
                                          CashFlowAccount provider, String sourceAccountTypeValue,
                                          long destinationAccountId, CashFlowAccount destinationAccountType,
                                          String destinationAccountTypeValue, PaymentChangeType paymentChangeType) {
        CashFlow cashFlowPaymentAmount = new CashFlow();
        cashFlowPaymentAmount.setObjId(objId);
        cashFlowPaymentAmount.setAmount(amount);
        cashFlowPaymentAmount.setCurrencyCode(currencyCode);
        cashFlowPaymentAmount.setSourceAccountId(sourceAccountId);
        cashFlowPaymentAmount.setSourceAccountType(provider);
        cashFlowPaymentAmount.setSourceAccountTypeValue(sourceAccountTypeValue);
        cashFlowPaymentAmount.setDestinationAccountId(destinationAccountId);
        cashFlowPaymentAmount.setDestinationAccountType(destinationAccountType);
        cashFlowPaymentAmount.setDestinationAccountTypeValue(destinationAccountTypeValue);
        cashFlowPaymentAmount.setObjType(paymentChangeType);
        return cashFlowPaymentAmount;
    }

    public static FeeType getFeeType(CashFlow cashFlow) {
        CashFlowAccount source = cashFlow.getSourceAccountType();
        String sourceValue = cashFlow.getSourceAccountTypeValue();
        CashFlowAccount destination = cashFlow.getDestinationAccountType();
        String destinationValue = cashFlow.getDestinationAccountTypeValue();

        if (source == CashFlowAccount.merchant
                && sourceValue.equals(MerchantCashFlowAccount.settlement.name())
                && destination == CashFlowAccount.system
                && destinationValue.equals(SystemCashFlowAccount.settlement.name())) {
            return FeeType.FEE;
        }

        if (source == CashFlowAccount.system
                && sourceValue.equals(SystemCashFlowAccount.settlement.name())
                && destination == CashFlowAccount.external
                && (destinationValue.equals(ExternalCashFlowAccount.income.name())
                || destinationValue.equals(ExternalCashFlowAccount.outcome.name()))) {
            return FeeType.EXTERNAL_FEE;
        }

        if (source == CashFlowAccount.system
                && sourceValue.equals(SystemCashFlowAccount.settlement.name())
                && destination == CashFlowAccount.provider
                && destinationValue.equals(ProviderCashFlowAccount.settlement.name())
        ) {
            return FeeType.PROVIDER_FEE;
        }

        if (source == CashFlowAccount.merchant
                && sourceValue.equals(MerchantCashFlowAccount.settlement.name())
                && destination == CashFlowAccount.merchant
                && destinationValue.equals(MerchantCashFlowAccount.guarantee.name())
        ) {
            return FeeType.GUARANTEE_DEPOSIT;
        }

        return FeeType.UNKNOWN;
    }

    public enum FeeType {
        UNKNOWN,
        FEE,
        PROVIDER_FEE,
        EXTERNAL_FEE,
        GUARANTEE_DEPOSIT
    }
}
