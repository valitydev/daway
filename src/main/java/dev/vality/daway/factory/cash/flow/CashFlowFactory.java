package dev.vality.daway.factory.cash.flow;

import dev.vality.damsel.domain.FinalCashFlowAccount;
import dev.vality.damsel.domain.FinalCashFlowPosting;
import dev.vality.daway.domain.enums.AdjustmentCashFlowType;
import dev.vality.daway.domain.enums.PaymentChangeType;
import dev.vality.daway.domain.tables.pojos.CashFlow;
import dev.vality.geck.common.util.TypeUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CashFlowFactory {

    public static List<CashFlow> build(List<FinalCashFlowPosting> cashFlowPostings, Long objId,
                                                  PaymentChangeType paymentChangeType) {
        return build(cashFlowPostings, objId, paymentChangeType, null);
    }

    public static List<CashFlow> build(List<FinalCashFlowPosting> cashFlowPostings, Long objId,
                                                  PaymentChangeType paymentChangeType,
                                                  AdjustmentCashFlowType adjustmentCashFlowType) {
        return cashFlowPostings.stream().map(cf -> {
            CashFlow pcf = new CashFlow();
            pcf.setObjId(objId);
            pcf.setObjType(paymentChangeType);
            pcf.setAdjFlowType(adjustmentCashFlowType);
            pcf.setSourceAccountType(getCashFlowAccountType(cf.getSource()));
            pcf.setSourceAccountTypeValue(getCashFlowAccountTypeValue(cf.getSource()));
            pcf.setSourceAccountId(cf.getSource().getAccountId());
            pcf.setDestinationAccountType(getCashFlowAccountType(cf.getDestination()));
            pcf.setDestinationAccountTypeValue(getCashFlowAccountTypeValue(cf.getDestination()));
            pcf.setDestinationAccountId(cf.getDestination().getAccountId());
            pcf.setAmount(cf.getVolume().getAmount());
            pcf.setCurrencyCode(cf.getVolume().getCurrency().getSymbolicCode());
            pcf.setDetails(cf.getDetails());
            return pcf;
        }).collect(Collectors.toList());
    }

    private static dev.vality.daway.domain.enums.CashFlowAccount getCashFlowAccountType(FinalCashFlowAccount cfa) {
        dev.vality.daway.domain.enums.CashFlowAccount sourceAccountType =
                TypeUtil.toEnumField(cfa.getAccountType().getSetField().getFieldName(), dev.vality.daway.domain.enums.CashFlowAccount.class);
        if (sourceAccountType == null) {
            throw new IllegalArgumentException("Illegal cash flow account type: " + cfa.getAccountType());
        }
        return sourceAccountType;
    }

    private static String getCashFlowAccountTypeValue(FinalCashFlowAccount cfa) {
        if (cfa.getAccountType().isSetMerchant()) {
            return cfa.getAccountType().getMerchant().name();
        } else if (cfa.getAccountType().isSetProvider()) {
            return cfa.getAccountType().getProvider().name();
        } else if (cfa.getAccountType().isSetSystem()) {
            return cfa.getAccountType().getSystem().name();
        } else if (cfa.getAccountType().isSetExternal()) {
            return cfa.getAccountType().getExternal().name();
        } else if (cfa.getAccountType().isSetWallet()) {
            return cfa.getAccountType().getWallet().name();
        } else {
            throw new IllegalArgumentException("Illegal cash flow account type: " + cfa.getAccountType());
        }
    }

}
