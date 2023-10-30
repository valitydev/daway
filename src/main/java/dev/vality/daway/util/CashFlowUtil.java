package dev.vality.daway.util;

import dev.vality.damsel.domain.*;
import dev.vality.daway.model.CashFlowType;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CashFlowUtil {

    public static Long computeMerchantAmount(List<FinalCashFlowPosting> finalCashFlow) {
        long amountSource = computeMerchantAmount(finalCashFlow, FinalCashFlowPosting::getSource);
        long amountDest = computeMerchantAmount(finalCashFlow, FinalCashFlowPosting::getDestination);
        return amountDest - amountSource;
    }

    public static long computeMerchantAmount(List<FinalCashFlowPosting> finalCashFlow,
                                             Function<FinalCashFlowPosting, FinalCashFlowAccount> func) {
        return computeAmount(finalCashFlow, f -> isMerchantSettlement(func.apply(f).getAccountType()));
    }

    public static Long computeProviderAmount(List<FinalCashFlowPosting> finalCashFlow) {
        long amountSource = computeProviderAmount(finalCashFlow, FinalCashFlowPosting::getSource);
        long amountDest = computeProviderAmount(finalCashFlow, FinalCashFlowPosting::getDestination);
        return amountDest - amountSource;
    }

    public static long computeProviderAmount(List<FinalCashFlowPosting> finalCashFlow,
                                             Function<FinalCashFlowPosting, FinalCashFlowAccount> func) {
        return computeAmount(finalCashFlow, f -> isProviderSettlement(func.apply(f).getAccountType()));
    }

    public static Long computeSystemAmount(List<FinalCashFlowPosting> finalCashFlow) {
        long amountSource = computeSystemAmount(finalCashFlow, FinalCashFlowPosting::getSource);
        long amountDest = computeSystemAmount(finalCashFlow, FinalCashFlowPosting::getDestination);
        return amountDest - amountSource;
    }

    public static long computeSystemAmount(List<FinalCashFlowPosting> finalCashFlow,
                                           Function<FinalCashFlowPosting, FinalCashFlowAccount> func) {
        return computeAmount(finalCashFlow, f -> isSystemSettlement(func.apply(f).getAccountType()));
    }

    public static Long computeExternalIncomeAmount(List<FinalCashFlowPosting> finalCashFlow) {
        long amountSource = computeExternalIncomeAmount(finalCashFlow, FinalCashFlowPosting::getSource);
        long amountDest = computeExternalIncomeAmount(finalCashFlow, FinalCashFlowPosting::getDestination);
        return amountDest - amountSource;
    }

    public static long computeExternalIncomeAmount(List<FinalCashFlowPosting> finalCashFlow,
                                                   Function<FinalCashFlowPosting, FinalCashFlowAccount> func) {
        return computeAmount(finalCashFlow, f -> isExternalIncome(func.apply(f).getAccountType()));
    }

    public static Long computeExternalOutcomeAmount(List<FinalCashFlowPosting> finalCashFlow) {
        long amountSource = computeExternalOutcomeAmount(finalCashFlow, FinalCashFlowPosting::getSource);
        long amountDest = computeExternalOutcomeAmount(finalCashFlow, FinalCashFlowPosting::getDestination);
        return amountDest - amountSource;
    }

    public static long computeExternalOutcomeAmount(List<FinalCashFlowPosting> finalCashFlow,
                                                    Function<FinalCashFlowPosting, FinalCashFlowAccount> func) {
        return computeAmount(finalCashFlow, f -> isExternalOutcome(func.apply(f).getAccountType()));
    }

    private static long computeAmount(List<FinalCashFlowPosting> finalCashFlow,
                                      Predicate<FinalCashFlowPosting> filter) {
        return finalCashFlow.stream()
                .filter(filter)
                .mapToLong(cashFlow -> cashFlow.getVolume().getAmount())
                .sum();
    }

    private static boolean isMerchantSettlement(dev.vality.damsel.domain.CashFlowAccount cashFlowAccount) {
        return cashFlowAccount.isSetMerchant()
                && cashFlowAccount.getMerchant() == MerchantCashFlowAccount.settlement;
    }

    private static boolean isProviderSettlement(dev.vality.damsel.domain.CashFlowAccount cashFlowAccount) {
        return cashFlowAccount.isSetProvider()
                && cashFlowAccount.getProvider() == ProviderCashFlowAccount.settlement;
    }

    private static boolean isSystemSettlement(dev.vality.damsel.domain.CashFlowAccount cashFlowAccount) {
        return cashFlowAccount.isSetSystem()
                && cashFlowAccount.getSystem() == SystemCashFlowAccount.settlement;
    }

    private static boolean isExternalIncome(dev.vality.damsel.domain.CashFlowAccount cashFlowAccount) {
        return cashFlowAccount.isSetExternal()
                && cashFlowAccount.getExternal() == ExternalCashFlowAccount.income;
    }

    private static boolean isExternalOutcome(dev.vality.damsel.domain.CashFlowAccount cashFlowAccount) {
        return cashFlowAccount.isSetExternal()
                && cashFlowAccount.getExternal() == ExternalCashFlowAccount.outcome;
    }

    public static Map<CashFlowType, Long> parseCashFlow(List<FinalCashFlowPosting> finalCashFlow) {
        return parseCashFlow(finalCashFlow, CashFlowType::getCashFlowType);
    }

    private static Map<CashFlowType, Long> parseCashFlow(List<FinalCashFlowPosting> finalCashFlow,
                                                         Function<FinalCashFlowPosting, CashFlowType> classifier) {
        return finalCashFlow.stream()
                .collect(
                        Collectors.groupingBy(
                                classifier,
                                Collectors.summingLong(cashFlow -> cashFlow.getVolume().getAmount()
                                )
                        )
                );
    }
}
