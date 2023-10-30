package dev.vality.daway.util;

import dev.vality.damsel.domain.FinalCashFlowAccount;
import dev.vality.damsel.domain.FinalCashFlowPosting;
import dev.vality.damsel.domain.InvoicePaymentAdjustment;
import dev.vality.daway.TestData;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class AdjustmentUtilsTest {

    @Test
    public void calculateMerchantAmountDiffTest() {
        List<FinalCashFlowPosting> oldCashFlow = createOldCashFlow(TestData.createMerchantAccount());
        long oldAmountSource = CashFlowUtil.computeMerchantAmount(oldCashFlow, FinalCashFlowPosting::getSource);
        long oldAmountDest = CashFlowUtil.computeMerchantAmount(oldCashFlow, FinalCashFlowPosting::getDestination);
        assertEquals(30, oldAmountSource);
        assertEquals(15, oldAmountDest);
        assertEquals(-15, CashFlowUtil.computeMerchantAmount(oldCashFlow));

        List<FinalCashFlowPosting> newCashFlow = createNewCashFlow(TestData.createMerchantAccount());
        long newAmountSource = CashFlowUtil.computeMerchantAmount(newCashFlow, FinalCashFlowPosting::getSource);
        long newAmountDest = CashFlowUtil.computeMerchantAmount(newCashFlow, FinalCashFlowPosting::getDestination);
        assertEquals(32, newAmountSource);
        assertEquals(13, newAmountDest);
        assertEquals(-19, CashFlowUtil.computeMerchantAmount(newCashFlow));

        InvoicePaymentAdjustment adjustment = TestData.createTestInvoicePaymentAdjustment(oldCashFlow, newCashFlow);
        assertEquals(-34, AdjustmentUtils.calculateMerchantAmountDiff(adjustment));
    }

    @Test
    public void calculateProviderAmountDiffTest() {
        List<FinalCashFlowPosting> oldCashFlow = createOldCashFlow(TestData.createProviderAccount());
        long oldAmountSource = CashFlowUtil.computeProviderAmount(oldCashFlow, FinalCashFlowPosting::getSource);
        long oldAmountDest = CashFlowUtil.computeProviderAmount(oldCashFlow, FinalCashFlowPosting::getDestination);
        assertEquals(30, oldAmountSource);
        assertEquals(15, oldAmountDest);
        assertEquals(-15, CashFlowUtil.computeProviderAmount(oldCashFlow));

        List<FinalCashFlowPosting> newCashFlow = createNewCashFlow(TestData.createProviderAccount());
        long newAmountSource = CashFlowUtil.computeProviderAmount(newCashFlow, FinalCashFlowPosting::getSource);
        long newAmountDest = CashFlowUtil.computeProviderAmount(newCashFlow, FinalCashFlowPosting::getDestination);
        assertEquals(32, newAmountSource);
        assertEquals(13, newAmountDest);
        assertEquals(-19, CashFlowUtil.computeProviderAmount(newCashFlow));

        InvoicePaymentAdjustment adjustment = TestData.createTestInvoicePaymentAdjustment(oldCashFlow, newCashFlow);
        assertEquals(-34, AdjustmentUtils.calculateProviderAmountDiff(adjustment));
    }

    @Test
    public void calculateSystemAmountDiffTest() {
        List<FinalCashFlowPosting> oldCashFlow = createOldCashFlow(TestData.createSystemAccount());
        long oldAmountSource = CashFlowUtil.computeSystemAmount(oldCashFlow, FinalCashFlowPosting::getSource);
        long oldAmountDest = CashFlowUtil.computeSystemAmount(oldCashFlow, FinalCashFlowPosting::getDestination);
        assertEquals(30, oldAmountSource);
        assertEquals(15, oldAmountDest);
        assertEquals(-15, CashFlowUtil.computeSystemAmount(oldCashFlow));

        List<FinalCashFlowPosting> newCashFlow = createNewCashFlow(TestData.createSystemAccount());
        long newAmountSource = CashFlowUtil.computeSystemAmount(newCashFlow, FinalCashFlowPosting::getSource);
        long newAmountDest = CashFlowUtil.computeSystemAmount(newCashFlow, FinalCashFlowPosting::getDestination);
        assertEquals(32, newAmountSource);
        assertEquals(13, newAmountDest);
        assertEquals(-19, CashFlowUtil.computeSystemAmount(newCashFlow));

        InvoicePaymentAdjustment adjustment = TestData.createTestInvoicePaymentAdjustment(oldCashFlow, newCashFlow);
        assertEquals(-34, AdjustmentUtils.calculateSystemAmountDiff(adjustment));
    }

    @Test
    public void calculateExternalIncomeAmountDiffTest() {
        List<FinalCashFlowPosting> oldCashFlow = createOldCashFlow(TestData.createExternalIncomeAccount());
        long oldAmountSource = CashFlowUtil.computeExternalIncomeAmount(oldCashFlow, FinalCashFlowPosting::getSource);
        long oldAmountDest =
                CashFlowUtil.computeExternalIncomeAmount(oldCashFlow, FinalCashFlowPosting::getDestination);
        assertEquals(30, oldAmountSource);
        assertEquals(15, oldAmountDest);
        assertEquals(-15, CashFlowUtil.computeExternalIncomeAmount(oldCashFlow));

        List<FinalCashFlowPosting> newCashFlow = createNewCashFlow(TestData.createExternalIncomeAccount());
        long newAmountSource = CashFlowUtil.computeExternalIncomeAmount(newCashFlow, FinalCashFlowPosting::getSource);
        long newAmountDest =
                CashFlowUtil.computeExternalIncomeAmount(newCashFlow, FinalCashFlowPosting::getDestination);
        assertEquals(32, newAmountSource);
        assertEquals(13, newAmountDest);
        assertEquals(-19, CashFlowUtil.computeExternalIncomeAmount(newCashFlow));

        InvoicePaymentAdjustment adjustment = TestData.createTestInvoicePaymentAdjustment(oldCashFlow, newCashFlow);
        assertEquals(-34, AdjustmentUtils.calculateExternalIncomeAmountDiff(adjustment));
    }

    @Test
    public void calculateExternalOutcomeAmountDiffTest() {
        List<FinalCashFlowPosting> oldCashFlow = createOldCashFlow(TestData.createExternalOutcomeAccount());
        long oldAmountSource = CashFlowUtil.computeExternalOutcomeAmount(oldCashFlow, FinalCashFlowPosting::getSource);
        long oldAmountDest =
                CashFlowUtil.computeExternalOutcomeAmount(oldCashFlow, FinalCashFlowPosting::getDestination);
        assertEquals(30, oldAmountSource);
        assertEquals(15, oldAmountDest);
        assertEquals(-15, CashFlowUtil.computeExternalOutcomeAmount(oldCashFlow));

        List<FinalCashFlowPosting> newCashFlow = createNewCashFlow(TestData.createExternalOutcomeAccount());
        long newAmountSource = CashFlowUtil.computeExternalOutcomeAmount(newCashFlow, FinalCashFlowPosting::getSource);
        long newAmountDest =
                CashFlowUtil.computeExternalOutcomeAmount(newCashFlow, FinalCashFlowPosting::getDestination);
        assertEquals(32, newAmountSource);
        assertEquals(13, newAmountDest);
        assertEquals(-19, CashFlowUtil.computeExternalOutcomeAmount(newCashFlow));

        InvoicePaymentAdjustment adjustment = TestData.createTestInvoicePaymentAdjustment(oldCashFlow, newCashFlow);
        assertEquals(-34, AdjustmentUtils.calculateExternalOutcomeAmountDiff(adjustment));
    }

    private static List<FinalCashFlowPosting> createOldCashFlow(FinalCashFlowAccount account) {
        List<FinalCashFlowPosting> oldCashFlow = new ArrayList<>();
        oldCashFlow.add(new FinalCashFlowPosting(TestData.createSubagentAccount(), account, TestData.createTestCash(5)));
        oldCashFlow.add(new FinalCashFlowPosting(TestData.createSubagentAccount(), account, TestData.createTestCash(10)));
        oldCashFlow.add(new FinalCashFlowPosting(account, TestData.createSubagentAccount(), TestData.createTestCash(13)));
        oldCashFlow.add(new FinalCashFlowPosting(account, TestData.createSubagentAccount(), TestData.createTestCash(17)));
        return oldCashFlow;
    }

    private static List<FinalCashFlowPosting> createNewCashFlow(FinalCashFlowAccount account) {
        List<FinalCashFlowPosting> oldCashFlow = new ArrayList<>();
        oldCashFlow.add(new FinalCashFlowPosting(TestData.createSubagentAccount(), account, TestData.createTestCash(4)));
        oldCashFlow.add(new FinalCashFlowPosting(TestData.createSubagentAccount(), account, TestData.createTestCash(9)));
        oldCashFlow.add(new FinalCashFlowPosting(account, TestData.createSubagentAccount(), TestData.createTestCash(14)));
        oldCashFlow.add(new FinalCashFlowPosting(account, TestData.createSubagentAccount(), TestData.createTestCash(18)));
        return oldCashFlow;
    }

}
