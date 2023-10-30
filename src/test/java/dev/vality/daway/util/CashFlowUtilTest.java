package dev.vality.daway.util;

import dev.vality.damsel.domain.*;
import dev.vality.daway.model.CashFlowType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CashFlowUtilTest {

    @Test
    public void testCorrectCashFlowPostings() {
        for (CashFlowType cashFlowType : CashFlowType.values()) {
            for (CashFlowAccount sourceAccount : cashFlowType.getSources()) {
                for (CashFlowAccount destinationAccount : cashFlowType.getDestinations()) {
                    Assertions.assertEquals(cashFlowType, CashFlowType.getCashFlowType(
                            new FinalCashFlowPosting(
                                    new FinalCashFlowAccount(sourceAccount, 1),
                                    new FinalCashFlowAccount(destinationAccount, 2),
                                    new Cash(5, new CurrencyRef("UGA"))
                            )
                    ));
                }
            }
        }
    }

    @Test
    public void testCorrectCashFlowPostingsWithCashFlowAccount() {
        for (CashFlowType cashFlowType : CashFlowType.values()) {
            for (CashFlowAccount sourceAccount : cashFlowType.getSources()) {
                for (CashFlowAccount destinationAccount : cashFlowType.getDestinations()) {
                    Assertions.assertEquals(cashFlowType, CashFlowType.getCashFlowType(sourceAccount, destinationAccount));
                }
            }
        }
    }

    @Test
    public void testIncorrectCashFlowPostings() {
        Assertions.assertEquals(CashFlowType.UNKNOWN, CashFlowType.getCashFlowType(
                new FinalCashFlowPosting(
                        new FinalCashFlowAccount(CashFlowAccount.provider(ProviderCashFlowAccount.settlement),
                                1),
                        new FinalCashFlowAccount(CashFlowAccount.merchant(MerchantCashFlowAccount.guarantee),
                                2),
                        new Cash(5, new CurrencyRef("UGA"))
                )
        ));
    }

}
