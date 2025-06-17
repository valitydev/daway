package dev.vality.daway;

import dev.vality.damsel.domain.*;
import dev.vality.damsel.domain.CashFlowAccount;
import dev.vality.damsel.domain.InvoicePaymentChargeback;
import dev.vality.damsel.domain.LegalEntity;
import dev.vality.damsel.payment_processing.*;
import dev.vality.damsel.payment_processing.SessionChangePayload;
import dev.vality.damsel.payment_processing.SessionFinished;
import dev.vality.damsel.payment_processing.SessionResult;
import dev.vality.damsel.payment_processing.SessionSucceeded;
import dev.vality.damsel.user_interaction.BrowserGetRequest;
import dev.vality.damsel.user_interaction.BrowserHTTPRequest;
import dev.vality.damsel.user_interaction.UserInteraction;
import dev.vality.daway.domain.enums.*;
import dev.vality.daway.domain.tables.pojos.Chargeback;
import dev.vality.daway.domain.tables.pojos.FistfulCashFlow;
import dev.vality.daway.domain.tables.pojos.WithdrawalAdjustment;
import dev.vality.fistful.account.Account;
import dev.vality.fistful.base.Realm;
import dev.vality.fistful.cashflow.FinalCashFlow;
import dev.vality.fistful.transfer.Committed;
import dev.vality.fistful.transfer.Transfer;
import dev.vality.fistful.withdrawal.*;
import dev.vality.fistful.withdrawal.Change;
import dev.vality.fistful.withdrawal.adjustment.*;
import dev.vality.fistful.withdrawal.adjustment.CreatedChange;
import dev.vality.fistful.withdrawal.adjustment.StatusChange;
import dev.vality.fistful.withdrawal.adjustment.TransferChange;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.kafka.common.serialization.ThriftSerializer;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.machinegun.msgpack.Value;
import dev.vality.sink.common.serialization.impl.PaymentEventPayloadSerializer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestData {

    public static final String OCCURED_AT = "2023-07-03T10:15:30Z";

    public static InvoiceChange buildInvoiceChangeChargebackCreated() {
        InvoicePaymentChargeback invoicePaymentChargeback =
                dev.vality.testcontainers.annotations.util.RandomBeans.random(InvoicePaymentChargeback.class, "context",
                        "status", "reason", "stage");
        invoicePaymentChargeback.setCreatedAt(TypeUtil.temporalToString(Instant.now()));
        InvoicePaymentChargebackStatus invoicePaymentChargebackStatus = buildChargebackStatus();
        invoicePaymentChargeback.setStatus(invoicePaymentChargebackStatus);

        InvoicePaymentChargebackReason invoicePaymentChargebackReason = buildChargebackReason();
        invoicePaymentChargeback.setReason(invoicePaymentChargebackReason);

        InvoicePaymentChargebackStage invoicePaymentChargebackStage = buildChargebackStage();
        invoicePaymentChargeback.setStage(invoicePaymentChargebackStage);

        InvoicePaymentChargebackChange invoicePaymentChargebackChange = new InvoicePaymentChargebackChange();
        invoicePaymentChargebackChange.setId("testChargebackId");
        InvoicePaymentChargebackChangePayload invoicePaymentChargebackChangePayload =
                new InvoicePaymentChargebackChangePayload();
        InvoicePaymentChargebackCreated invoicePaymentChargebackCreated = new InvoicePaymentChargebackCreated();
        invoicePaymentChargebackCreated.setChargeback(invoicePaymentChargeback);

        invoicePaymentChargebackChangePayload.setInvoicePaymentChargebackCreated(invoicePaymentChargebackCreated);
        invoicePaymentChargebackChange.setPayload(invoicePaymentChargebackChangePayload);

        InvoicePaymentChangePayload invoicePaymentChangePayload = new InvoicePaymentChangePayload();
        invoicePaymentChangePayload.setInvoicePaymentChargebackChange(invoicePaymentChargebackChange);

        InvoicePaymentChange invoicePaymentChange = new InvoicePaymentChange();
        invoicePaymentChange.setId("testPaymentId");
        invoicePaymentChange.setPayload(invoicePaymentChangePayload);

        InvoiceChange invoiceChange = new InvoiceChange();
        invoiceChange.setInvoicePaymentChange(invoicePaymentChange);

        return invoiceChange;
    }

    public static InvoiceChange buildInvoiceChangeChargebackStatusChanged() {
        InvoicePaymentChargebackStatusChanged invoicePaymentChargebackStatusChanged =
                new InvoicePaymentChargebackStatusChanged();
        invoicePaymentChargebackStatusChanged.setStatus(buildChargebackStatus());
        InvoicePaymentChargebackChangePayload invoicePaymentChargebackChangePayload =
                new InvoicePaymentChargebackChangePayload();
        invoicePaymentChargebackChangePayload
                .setInvoicePaymentChargebackStatusChanged(invoicePaymentChargebackStatusChanged);

        return buildInvoiceChangeChargeback(invoicePaymentChargebackChangePayload);
    }

    public static InvoiceChange buildInvoiceChangeChargebackLevyChanged() {
        InvoicePaymentChargebackLevyChanged invoicePaymentChargebackLevyChanged =
                new InvoicePaymentChargebackLevyChanged();
        Cash cash = new Cash().setAmount(1000L).setCurrency(new CurrencyRef().setSymbolicCode("456"));
        invoicePaymentChargebackLevyChanged.setLevy(cash);
        InvoicePaymentChargebackChangePayload invoicePaymentChargebackChangePayload =
                new InvoicePaymentChargebackChangePayload();
        invoicePaymentChargebackChangePayload
                .setInvoicePaymentChargebackLevyChanged(invoicePaymentChargebackLevyChanged);

        return buildInvoiceChangeChargeback(invoicePaymentChargebackChangePayload);
    }

    public static InvoiceChange buildInvoiceChangeChargebackStageChanged() {
        InvoicePaymentChargebackStageChanged invoicePaymentChargebackStageChanged =
                new InvoicePaymentChargebackStageChanged();
        invoicePaymentChargebackStageChanged.setStage(buildChargebackStage());
        InvoicePaymentChargebackChangePayload invoicePaymentChargebackChangePayload =
                new InvoicePaymentChargebackChangePayload();
        invoicePaymentChargebackChangePayload
                .setInvoicePaymentChargebackStageChanged(invoicePaymentChargebackStageChanged);

        return buildInvoiceChangeChargeback(invoicePaymentChargebackChangePayload);
    }

    public static InvoiceChange buildInvoiceChangeChargebackCashFlowChanged() {
        InvoicePaymentChargebackCashFlowChanged invoicePaymentChargebackCashFlowChanged =
                new InvoicePaymentChargebackCashFlowChanged();
        invoicePaymentChargebackCashFlowChanged.setCashFlow(Collections.singletonList(buildCashFlowPosting()));
        InvoicePaymentChargebackChangePayload invoicePaymentChargebackChangePayload =
                new InvoicePaymentChargebackChangePayload();
        invoicePaymentChargebackChangePayload
                .setInvoicePaymentChargebackCashFlowChanged(invoicePaymentChargebackCashFlowChanged);

        return buildInvoiceChangeChargeback(invoicePaymentChargebackChangePayload);
    }

    public static InvoiceChange buildInvoiceChangeChargebackCashFlowChanged(List<FinalCashFlowPosting> postings) {
        InvoicePaymentChargebackCashFlowChanged invoicePaymentChargebackCashFlowChanged =
                new InvoicePaymentChargebackCashFlowChanged();
        invoicePaymentChargebackCashFlowChanged.setCashFlow(postings);
        InvoicePaymentChargebackChangePayload invoicePaymentChargebackChangePayload =
                new InvoicePaymentChargebackChangePayload();
        invoicePaymentChargebackChangePayload
                .setInvoicePaymentChargebackCashFlowChanged(invoicePaymentChargebackCashFlowChanged);

        return buildInvoiceChangeChargeback(invoicePaymentChargebackChangePayload);
    }

    public static InvoiceChange buildInvoiceChangeChargebackBodyChanged() {
        InvoicePaymentChargebackBodyChanged invoicePaymentChargebackBodyChanged =
                new InvoicePaymentChargebackBodyChanged();
        Cash cash = new Cash().setAmount(1000).setCurrency(new CurrencyRef("653"));
        invoicePaymentChargebackBodyChanged.setBody(cash);
        InvoicePaymentChargebackChangePayload invoicePaymentChargebackChangePayload =
                new InvoicePaymentChargebackChangePayload();
        invoicePaymentChargebackChangePayload
                .setInvoicePaymentChargebackBodyChanged(invoicePaymentChargebackBodyChanged);

        return buildInvoiceChangeChargeback(invoicePaymentChargebackChangePayload);
    }

    private static InvoiceChange buildInvoiceChangeChargeback(InvoicePaymentChargebackChangePayload payload) {
        InvoicePaymentChargeback invoicePaymentChargeback =
                dev.vality.testcontainers.annotations.util.RandomBeans.random(InvoicePaymentChargeback.class, "context",
                        "status", "reason", "stage");
        invoicePaymentChargeback.setCreatedAt(TypeUtil.temporalToString(Instant.now()));
        InvoicePaymentChargebackStatus invoicePaymentChargebackStatus = buildChargebackStatus();
        invoicePaymentChargeback.setStatus(invoicePaymentChargebackStatus);

        InvoicePaymentChargebackReason invoicePaymentChargebackReason = buildChargebackReason();
        invoicePaymentChargeback.setReason(invoicePaymentChargebackReason);

        InvoicePaymentChargebackStage invoicePaymentChargebackStage = buildChargebackStage();
        invoicePaymentChargeback.setStage(invoicePaymentChargebackStage);

        InvoicePaymentChargebackChange invoicePaymentChargebackChange = new InvoicePaymentChargebackChange();
        invoicePaymentChargebackChange.setId("testChargebackId");
        invoicePaymentChargebackChange.setPayload(payload);

        InvoicePaymentChangePayload invoicePaymentChangePayload = new InvoicePaymentChangePayload();
        invoicePaymentChangePayload.setInvoicePaymentChargebackChange(invoicePaymentChargebackChange);

        InvoicePaymentChange invoicePaymentChange = new InvoicePaymentChange();
        invoicePaymentChange.setId("testPaymentId");
        invoicePaymentChange.setPayload(invoicePaymentChangePayload);

        InvoiceChange invoiceChange = new InvoiceChange();
        invoiceChange.setInvoicePaymentChange(invoicePaymentChange);

        return invoiceChange;
    }

    private static FinalCashFlowPosting buildCashFlowPosting() {
        FinalCashFlowAccount sourceFinalCashFlowAccount = new FinalCashFlowAccount();
        sourceFinalCashFlowAccount.setAccountId(12345);
        sourceFinalCashFlowAccount.setAccountType(CashFlowAccount.merchant(MerchantCashFlowAccount.payout));
        FinalCashFlowAccount destFinalCashFlowAccount = new FinalCashFlowAccount();
        destFinalCashFlowAccount.setAccountId(56789);
        destFinalCashFlowAccount.setAccountType(CashFlowAccount.provider(ProviderCashFlowAccount.settlement));
        FinalCashFlowPosting cashFlowPosting = new FinalCashFlowPosting();
        cashFlowPosting.setDetails("testDetails");
        cashFlowPosting.setSource(sourceFinalCashFlowAccount);
        cashFlowPosting.setDestination(destFinalCashFlowAccount);
        Cash cash = new Cash();
        cash.setAmount(1000);
        cash.setCurrency(new CurrencyRef("643"));
        cashFlowPosting.setVolume(cash);

        return cashFlowPosting;
    }

    private static InvoicePaymentChargebackReason buildChargebackReason() {
        InvoicePaymentChargebackReason invoicePaymentChargebackReason = new InvoicePaymentChargebackReason();
        invoicePaymentChargebackReason.setCode("testCode");
        InvoicePaymentChargebackCategory invoicePaymentChargebackCategory = new InvoicePaymentChargebackCategory();
        invoicePaymentChargebackCategory.setFraud(new InvoicePaymentChargebackCategoryFraud());
        invoicePaymentChargebackReason.setCategory(invoicePaymentChargebackCategory);

        return invoicePaymentChargebackReason;
    }

    private static InvoicePaymentChargebackStatus buildChargebackStatus() {
        InvoicePaymentChargebackStatus invoicePaymentChargebackStatus = new InvoicePaymentChargebackStatus();
        invoicePaymentChargebackStatus.setAccepted(new InvoicePaymentChargebackAccepted());

        return invoicePaymentChargebackStatus;
    }

    private static InvoicePaymentChargebackStage buildChargebackStage() {
        InvoicePaymentChargebackStage invoicePaymentChargebackStage = new InvoicePaymentChargebackStage();
        invoicePaymentChargebackStage.setChargeback(new InvoicePaymentChargebackStageChargeback());

        return invoicePaymentChargebackStage;
    }

    public static Contractor buildContractor() {
        Contractor contractor = new Contractor();
        LegalEntity legalEntity = new LegalEntity();
        contractor.setLegalEntity(legalEntity);
        InternationalLegalEntity internationalLegalEntity = new InternationalLegalEntity();
        legalEntity.setInternationalLegalEntity(internationalLegalEntity);
        internationalLegalEntity
                .setCountry(new CountryRef().setId(CountryCode.findByValue(CountryCode.AUT.getValue())));
        internationalLegalEntity.setLegalName(randomString());
        internationalLegalEntity.setActualAddress(randomString());
        internationalLegalEntity.setRegisteredAddress(randomString());
        return contractor;
    }

    public static String randomString() {
        return UUID.randomUUID().toString();
    }

    public static CountryObject buildCountryObject() {
        Country country = new Country();
        country.setName(randomString());
        country.setTradeBlocs(Set.of(new TradeBlocRef().setId(randomString())));
        CountryObject countryObject = new CountryObject();
        countryObject.setData(country);
        countryObject.setRef(new CountryRef().setId(CountryCode.ABH));
        return countryObject;
    }

    public static TradeBlocObject buildTradeBlocObject() {
        TradeBloc tradeBloc = new TradeBloc();
        tradeBloc.setName(randomString());
        tradeBloc.setDescription(randomString());
        TradeBlocObject tradeBlocObject = new TradeBlocObject();
        tradeBlocObject.setData(tradeBloc);
        tradeBlocObject.setRef(new TradeBlocRef().setId(randomString()));
        return tradeBlocObject;
    }

    public static InvoicePaymentAdjustment createTestInvoicePaymentAdjustment(List<FinalCashFlowPosting> oldCashFlow,
                                                                              List<FinalCashFlowPosting> newCashFlow) {
        InvoicePaymentAdjustment adjustment = new InvoicePaymentAdjustment();
        adjustment.setId("Adj1");
        adjustment.setState(InvoicePaymentAdjustmentState.cash_flow(
                new InvoicePaymentAdjustmentCashFlowState()
                        .setScenario(new InvoicePaymentAdjustmentCashFlow().setDomainRevision(1))
        ));
        adjustment.setCreatedAt(Instant.now().toString());
        adjustment.setDomainRevision(1L);
        adjustment.setReason("Test");
        adjustment.setNewCashFlow(newCashFlow);
        adjustment.setOldCashFlowInverse(oldCashFlow);
        adjustment.setPartyRevision(1L);
        adjustment.setStatus(InvoicePaymentAdjustmentStatus.captured(new InvoicePaymentAdjustmentCaptured()));
        return adjustment;
    }

    public static FinalCashFlowAccount createMerchantAccount() {
        return new FinalCashFlowAccount(CashFlowAccount.merchant(MerchantCashFlowAccount.settlement), 2);
    }

    public static FinalCashFlowAccount createProviderAccount() {
        return new FinalCashFlowAccount(CashFlowAccount.provider(ProviderCashFlowAccount.settlement), 2);
    }

    public static FinalCashFlowAccount createSystemAccount() {
        return new FinalCashFlowAccount(CashFlowAccount.system(SystemCashFlowAccount.settlement), 1);
    }

    public static FinalCashFlowAccount createSubagentAccount() {
        return new FinalCashFlowAccount(CashFlowAccount.system(SystemCashFlowAccount.subagent), 1);
    }

    public static FinalCashFlowAccount createExternalIncomeAccount() {
        return new FinalCashFlowAccount(CashFlowAccount.external(ExternalCashFlowAccount.income), 1);
    }

    public static FinalCashFlowAccount createExternalOutcomeAccount() {
        return new FinalCashFlowAccount(CashFlowAccount.external(ExternalCashFlowAccount.outcome), 1);
    }

    public static Cash createTestCash(long amount) {
        return new Cash(amount, new CurrencyRef("RUB"));
    }

    public static TimestampedChange createWithdrawalAdjustmentCreatedChange(String id) {
        Adjustment adjustment = new Adjustment();
        adjustment.setId(id);
        adjustment.setOperationTimestamp(OCCURED_AT);
        adjustment.setCreatedAt(OCCURED_AT);
        adjustment.setStatus(Status.pending(new Pending()));
        var newStatus = new dev.vality.fistful.withdrawal.status.Status();
        newStatus.setPending(new dev.vality.fistful.withdrawal.status.Pending());
        adjustment.setChangesPlan(
                new ChangesPlan()
                        .setNewStatus(new StatusChangePlan().setNewStatus(newStatus))
        );
        var payload = new dev.vality.fistful.withdrawal.adjustment.Change();
        payload.setCreated(new CreatedChange().setAdjustment(adjustment));
        AdjustmentChange adjustmentChange = new AdjustmentChange();
        adjustmentChange.setId("id");
        adjustmentChange.setPayload(payload);
        Change change = new Change();
        change.setAdjustment(adjustmentChange);
        TimestampedChange timestampedChange = new TimestampedChange();
        timestampedChange.setOccuredAt(OCCURED_AT);
        timestampedChange.setChange(change);
        return timestampedChange;
    }

    public static TimestampedChange createWithdrawalAdjustmentCreatedDomainRevisionChange(String id) {
        Adjustment adjustment = new Adjustment();
        adjustment.setId(id);
        adjustment.setOperationTimestamp(OCCURED_AT);
        adjustment.setCreatedAt(OCCURED_AT);
        adjustment.setStatus(Status.pending(new Pending()));
        var newStatus = new dev.vality.fistful.withdrawal.status.Status();
        newStatus.setPending(new dev.vality.fistful.withdrawal.status.Pending());
        adjustment.setChangesPlan(
                new ChangesPlan()
                        .setNewDomainRevision(new DataRevisionChangePlan().setNewDomainRevision(1L))
        );
        var payload = new dev.vality.fistful.withdrawal.adjustment.Change();
        payload.setCreated(new CreatedChange().setAdjustment(adjustment));
        AdjustmentChange adjustmentChange = new AdjustmentChange();
        adjustmentChange.setId("id");
        adjustmentChange.setPayload(payload);
        Change change = new Change();
        change.setAdjustment(adjustmentChange);
        TimestampedChange timestampedChange = new TimestampedChange();
        timestampedChange.setOccuredAt(OCCURED_AT);
        timestampedChange.setChange(change);
        return timestampedChange;
    }

    public static TimestampedChange createWithdrawalAdjustmentStatusChange(String id) {
        var payload = new dev.vality.fistful.withdrawal.adjustment.Change();
        payload.setStatusChanged(new StatusChange(Status.succeeded(new Succeeded())));
        AdjustmentChange adjustmentChange = new AdjustmentChange();
        adjustmentChange.setId(id);
        adjustmentChange.setPayload(payload);
        Change change = new Change();
        change.setAdjustment(adjustmentChange);
        TimestampedChange timestampedChange = new TimestampedChange();
        timestampedChange.setOccuredAt(OCCURED_AT);
        timestampedChange.setChange(change);
        return timestampedChange;
    }

    public static TimestampedChange createWithdrawalAdjustmentTransferCreatedChange(String id) {
        Transfer transfer = new Transfer();
        transfer.setId("id");
        List<dev.vality.fistful.cashflow.FinalCashFlowPosting> postings = getFinalCashFlowPostings();
        transfer.setCashflow(new FinalCashFlow().setPostings(postings));
        dev.vality.fistful.transfer.CreatedChange createdChange = new dev.vality.fistful.transfer.CreatedChange();
        createdChange.setTransfer(transfer);
        var payload = new dev.vality.fistful.withdrawal.adjustment.Change();
        payload.setTransfer(new TransferChange(dev.vality.fistful.transfer.Change.created(createdChange)));
        AdjustmentChange adjustmentChange = new AdjustmentChange();
        adjustmentChange.setId(id);
        adjustmentChange.setPayload(payload);
        Change change = new Change();
        change.setAdjustment(adjustmentChange);
        TimestampedChange timestampedChange = new TimestampedChange();
        timestampedChange.setOccuredAt(OCCURED_AT);
        timestampedChange.setChange(change);
        return timestampedChange;
    }

    private static List<dev.vality.fistful.cashflow.FinalCashFlowPosting> getFinalCashFlowPostings() {
        dev.vality.fistful.cashflow.FinalCashFlowPosting fistfulPosting =
                new dev.vality.fistful.cashflow.FinalCashFlowPosting();
        fistfulPosting.setDestination(
                new dev.vality.fistful.cashflow.FinalCashFlowAccount()
                        .setAccount(new Account()
                                .setRealm(Realm.test)
                                .setCurrency(new dev.vality.fistful.base.CurrencyRef("RUB"))
                                .setAccountId(1))
                        .setAccountType(dev.vality.fistful.cashflow.CashFlowAccount.system(
                                dev.vality.fistful.cashflow.SystemCashFlowAccount.settlement)));
        fistfulPosting.setSource(new dev.vality.fistful.cashflow.FinalCashFlowAccount()
                .setAccount(new Account()
                        .setRealm(Realm.test)
                        .setCurrency(new dev.vality.fistful.base.CurrencyRef("RUB"))
                        .setAccountId(2))
                .setAccountType(dev.vality.fistful.cashflow.CashFlowAccount.wallet(
                        dev.vality.fistful.cashflow.WalletCashFlowAccount.receiver_destination)));
        fistfulPosting.setVolume(new dev.vality.fistful.base.Cash()
                .setAmount(100L)
                .setCurrency(new dev.vality.fistful.base.CurrencyRef("RUB")));
        dev.vality.fistful.cashflow.FinalCashFlowPosting providerPosting =
                new dev.vality.fistful.cashflow.FinalCashFlowPosting();
        providerPosting.setDestination(
                new dev.vality.fistful.cashflow.FinalCashFlowAccount()
                        .setAccount(new Account()
                                .setRealm(Realm.test)
                                .setCurrency(new dev.vality.fistful.base.CurrencyRef("RUB"))
                                .setAccountId(3))
                        .setAccountType(dev.vality.fistful.cashflow.CashFlowAccount.provider(
                                dev.vality.fistful.cashflow.ProviderCashFlowAccount.settlement)));
        providerPosting.setSource(new dev.vality.fistful.cashflow.FinalCashFlowAccount()
                .setAccount(new Account()
                        .setRealm(Realm.test)
                        .setCurrency(new dev.vality.fistful.base.CurrencyRef("RUB"))
                        .setAccountId(4))
                .setAccountType(dev.vality.fistful.cashflow.CashFlowAccount.system(
                        dev.vality.fistful.cashflow.SystemCashFlowAccount.settlement)));
        providerPosting.setVolume(new dev.vality.fistful.base.Cash()
                .setAmount(100L)
                .setCurrency(new dev.vality.fistful.base.CurrencyRef("RUB")));
        dev.vality.fistful.cashflow.FinalCashFlowPosting merchantSourcePosting =
                new dev.vality.fistful.cashflow.FinalCashFlowPosting();
        merchantSourcePosting.setDestination(
                new dev.vality.fistful.cashflow.FinalCashFlowAccount()
                        .setAccount(new Account()
                                .setRealm(Realm.test)
                                .setCurrency(new dev.vality.fistful.base.CurrencyRef("RUB"))
                                .setAccountId(5))
                        .setAccountType(dev.vality.fistful.cashflow.CashFlowAccount.provider(
                                dev.vality.fistful.cashflow.ProviderCashFlowAccount.settlement)));
        merchantSourcePosting.setSource(new dev.vality.fistful.cashflow.FinalCashFlowAccount()
                .setAccount(new Account()
                        .setRealm(Realm.test)
                        .setCurrency(new dev.vality.fistful.base.CurrencyRef("RUB"))
                        .setAccountId(6))
                .setAccountType(dev.vality.fistful.cashflow.CashFlowAccount.merchant(
                        dev.vality.fistful.cashflow.MerchantCashFlowAccount.settlement)));
        merchantSourcePosting.setVolume(new dev.vality.fistful.base.Cash()
                .setAmount(200L)
                .setCurrency(new dev.vality.fistful.base.CurrencyRef("RUB")));
        dev.vality.fistful.cashflow.FinalCashFlowPosting merchantDestinationPosting =
                new dev.vality.fistful.cashflow.FinalCashFlowPosting();
        merchantDestinationPosting.setDestination(
                new dev.vality.fistful.cashflow.FinalCashFlowAccount()
                        .setAccount(new Account()
                                .setRealm(Realm.test)
                                .setCurrency(new dev.vality.fistful.base.CurrencyRef("RUB"))
                                .setAccountId(7))
                        .setAccountType(dev.vality.fistful.cashflow.CashFlowAccount.merchant(
                                dev.vality.fistful.cashflow.MerchantCashFlowAccount.settlement)));
        merchantDestinationPosting.setSource(new dev.vality.fistful.cashflow.FinalCashFlowAccount()
                .setAccount(new Account()
                        .setRealm(Realm.test)
                        .setCurrency(new dev.vality.fistful.base.CurrencyRef("RUB"))
                        .setAccountId(8))
                .setAccountType(dev.vality.fistful.cashflow.CashFlowAccount.merchant(
                        dev.vality.fistful.cashflow.MerchantCashFlowAccount.settlement)));
        merchantDestinationPosting.setVolume(new dev.vality.fistful.base.Cash()
                .setAmount(500)
                .setCurrency(new dev.vality.fistful.base.CurrencyRef("RUB")));
        return List.of(
                fistfulPosting,
                providerPosting,
                merchantSourcePosting,
                merchantDestinationPosting);
    }

    public static TimestampedChange createWithdrawalAdjustmentTransferStatusChange(String id) {
        dev.vality.fistful.transfer.StatusChange statusChange = new dev.vality.fistful.transfer.StatusChange();
        statusChange.setStatus(dev.vality.fistful.transfer.Status.committed(new Committed()));
        var payload = new dev.vality.fistful.withdrawal.adjustment.Change();
        payload.setTransfer(new TransferChange(dev.vality.fistful.transfer.Change.status_changed(statusChange)));
        AdjustmentChange adjustmentChange = new AdjustmentChange();
        adjustmentChange.setId(id);
        adjustmentChange.setPayload(payload);
        Change change = new Change();
        change.setAdjustment(adjustmentChange);
        TimestampedChange timestampedChange = new TimestampedChange();
        timestampedChange.setOccuredAt(OCCURED_AT);
        timestampedChange.setChange(change);
        return timestampedChange;
    }

    public static MachineEvent createMachineEvent(TimestampedChange timestampedChange) {
        return new MachineEvent()
                .setEventId(2L)
                .setSourceId("sourceId")
                .setSourceNs("2")
                .setCreatedAt("2021-05-31T06:12:27Z")
                .setData(Value.bin(new ThriftSerializer<>().serialize("", timestampedChange)));
    }

    public static MachineEvent createPartyEventDataMachineEvent(PartyEventData partyEventData, String id) {
        return new MachineEvent()
                .setEventId(2L)
                .setSourceId(id)
                .setSourceNs("2")
                .setCreatedAt("2021-05-31T06:12:27Z")
                .setData(Value.bin(new ThriftSerializer<>().serialize("", partyEventData)));
    }

    public static MachineEvent createMachineEvent(EventPayload eventPayload, String id) {
        return new MachineEvent()
                .setEventId(2L)
                .setSourceId(id)
                .setSourceNs("2")
                .setCreatedAt("2021-05-31T06:12:27Z")
                .setData(Value.bin(new ThriftSerializer<>().serialize("", eventPayload)));
    }

    public static WithdrawalAdjustment createWithdrawalAdjustment(String id) {
        WithdrawalAdjustment withdrawalAdjustment = new WithdrawalAdjustment();
        withdrawalAdjustment.setType(WithdrawalAdjustmentType.domain_revision);
        withdrawalAdjustment.setStatus(WithdrawalAdjustmentStatus.pending);
        withdrawalAdjustment.setSequenceId(1L);
        withdrawalAdjustment.setAdjustmentId(id);
        withdrawalAdjustment.setDomainRevision(1L);
        withdrawalAdjustment.setCurrent(true);
        withdrawalAdjustment.setWithdrawalId("withdrawalId");
        withdrawalAdjustment.setExternalId("id");
        withdrawalAdjustment.setEventOccuredAt(LocalDateTime.now());
        withdrawalAdjustment.setEventCreatedAt(LocalDateTime.now());
        withdrawalAdjustment.setWtime(LocalDateTime.now());
        return withdrawalAdjustment;
    }

    public static FistfulCashFlow createFistfulCashFlow() {
        FistfulCashFlow cashFlow = new FistfulCashFlow();
        cashFlow.setAmount(100L);
        cashFlow.setCurrencyCode("RUB");
        cashFlow.setObjType(FistfulCashFlowChangeType.withdrawal_adjustment);
        cashFlow.setDestinationAccountId("d_id");
        cashFlow.setDestinationAccountType(dev.vality.daway.domain.enums.CashFlowAccount.merchant);
        cashFlow.setDestinationAccountTypeValue("type");
        cashFlow.setSourceAccountTypeValue("type");
        cashFlow.setSourceAccountType(dev.vality.daway.domain.enums.CashFlowAccount.wallet);
        cashFlow.setSourceAccountId("s_id");
        return cashFlow;
    }

    public static TimestampedChange createWithdrawalCreatedChange(String id) {
        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setId(id);
        withdrawal.setDestinationId(randomString());
        withdrawal.setCreatedAt(OCCURED_AT);
        withdrawal.setWalletId(randomString());
        withdrawal.setPartyId(randomString());
        withdrawal.setBody(new dev.vality.fistful.base.Cash()
                .setAmount(100L)
                .setCurrency(new dev.vality.fistful.base.CurrencyRef()
                        .setSymbolicCode("RUB")));
        dev.vality.fistful.withdrawal.CreatedChange createdChange = new dev.vality.fistful.withdrawal.CreatedChange();
        createdChange.setWithdrawal(withdrawal);
        Change change = new Change();
        change.setCreated(createdChange);
        TimestampedChange timestampedChange = new TimestampedChange();
        timestampedChange.setOccuredAt(OCCURED_AT);
        timestampedChange.setChange(change);
        return timestampedChange;
    }

    public static MachineEvent createInvoice(InvoicePaymentChangePayload invoicePaymentChangePayload) {
        PaymentEventPayloadSerializer paymentEventPayloadSerializer = new PaymentEventPayloadSerializer();
        MachineEvent message = new MachineEvent();
        message.setCreatedAt(TypeUtil.temporalToString(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS)));
        message.setSourceId("source_id");
        EventPayload payload = new EventPayload();
        ArrayList<InvoiceChange> invoiceChanges = new ArrayList<>();
        InvoiceChange invoiceChange = new InvoiceChange();

        invoiceChange.setInvoicePaymentChange(new InvoicePaymentChange()
                .setPayload(invoicePaymentChangePayload)
                .setId("test"));
        invoiceChanges.add(invoiceChange);
        payload.setInvoiceChanges(invoiceChanges);
        Value data = new Value();
        data.setBin(paymentEventPayloadSerializer.serialize(payload));
        message.setData(data);
        return message;
    }

    @NotNull
    public static InvoicePaymentChangePayload createPaymentChange() {
        InvoicePaymentChangePayload invoicePaymentChangePayload = new InvoicePaymentChangePayload();
        TargetInvoicePaymentStatus targetInvoicePaymentStatus = new TargetInvoicePaymentStatus();
        targetInvoicePaymentStatus.setCaptured(new InvoicePaymentCaptured());
        SessionChangePayload sessionChangePayload = new SessionChangePayload();
        SessionResult sessionResult = new SessionResult();
        sessionResult.setSucceeded(new SessionSucceeded());
        sessionChangePayload.setSessionFinished(
                new SessionFinished().setResult(sessionResult));
        invoicePaymentChangePayload.setInvoicePaymentSessionChange(
                new InvoicePaymentSessionChange()
                        .setTarget(targetInvoicePaymentStatus)
                        .setPayload(sessionChangePayload));
        return invoicePaymentChangePayload;
    }

    @NotNull
    public static InvoicePaymentChangePayload createPaymentChangeRoute() {
        InvoicePaymentChangePayload invoicePaymentChangePayload = new InvoicePaymentChangePayload();
        invoicePaymentChangePayload.setInvoicePaymentRouteChanged(
                new InvoicePaymentRouteChanged()
                        .setRoute(new PaymentRoute()
                                .setTerminal(new TerminalRef()
                                        .setId(123))
                                .setProvider(new ProviderRef()
                                        .setId(321))));
        return invoicePaymentChangePayload;
    }

    public static InvoicePaymentChangePayload createInvoicePaymentSessionInteractionChange(
            UserInteraction userInteraction) {
        InvoicePaymentChangePayload invoicePaymentChangePayload = new InvoicePaymentChangePayload();
        TargetInvoicePaymentStatus targetInvoicePaymentStatus = new TargetInvoicePaymentStatus();
        targetInvoicePaymentStatus.setCaptured(new InvoicePaymentCaptured());
        SessionChangePayload sessionChangePayload = new SessionChangePayload();
        sessionChangePayload.setSessionInteractionChanged(
                new SessionInteractionChanged().setInteraction(userInteraction));
        invoicePaymentChangePayload.setInvoicePaymentSessionChange(
                new InvoicePaymentSessionChange()
                        .setTarget(targetInvoicePaymentStatus)
                        .setPayload(sessionChangePayload));
        return invoicePaymentChangePayload;
    }

    public static UserInteraction userInteraction() {
        UserInteraction userInteraction = new UserInteraction();
        BrowserHTTPRequest browserHTTPRequest = new BrowserHTTPRequest();
        browserHTTPRequest.setGetRequest(new BrowserGetRequest()
                .setUri("test.com"));
        userInteraction.setRedirect(browserHTTPRequest);
        return userInteraction;
    }

    public static PartyChange createPartyChangeWithPartyAdditionalInfoEffect(String name, String comment,
                                                                             String... emails) {
        AdditionalInfoEffect additionalInfoPartyNameEffect = AdditionalInfoEffect.party_name(name);
        ClaimEffect claimEffectPartyName = createAdditionalInfoEffect(additionalInfoPartyNameEffect);
        AdditionalInfoEffect additionalInfoCommentEffect = AdditionalInfoEffect.party_comment(comment);
        ClaimEffect claimEffectComment = createAdditionalInfoEffect(additionalInfoCommentEffect);
        AdditionalInfoEffect additionalInfoContactInfoEffect = AdditionalInfoEffect.contact_info(
                new PartyContactInfo()
                        .setManagerContactEmails(Arrays.stream(emails).toList())
                        .setRegistrationEmail("test@test.com")
        );
        ClaimEffect claimEffectContactInfo = createAdditionalInfoEffect(additionalInfoContactInfoEffect);
        ClaimAccepted claimAccepted = new ClaimAccepted();
        claimAccepted.setEffects(List.of(claimEffectPartyName, claimEffectComment, claimEffectContactInfo));
        ClaimStatusChanged claimStatusChanged = createClaimStatusChanged();
        claimStatusChanged.setStatus(ClaimStatus.accepted(claimAccepted));
        PartyChange partyChange = new PartyChange();
        partyChange.setClaimStatusChanged(claimStatusChanged);
        return partyChange;
    }

    @NotNull
    private static ClaimEffect createAdditionalInfoEffect(AdditionalInfoEffect additionalInfoPartyNameEffect) {
        AdditionalInfoEffectUnit additionalInfoEffectPartyNameUnit = new AdditionalInfoEffectUnit();
        additionalInfoEffectPartyNameUnit.setEffect(additionalInfoPartyNameEffect);
        ClaimEffect claimEffectPartyName = new ClaimEffect();
        claimEffectPartyName.setAdditionalInfoEffect(additionalInfoEffectPartyNameUnit);
        return claimEffectPartyName;
    }


    public static ClaimStatusChanged createClaimStatusChanged() {
        ClaimStatusChanged claimStatusChanged = new ClaimStatusChanged();
        claimStatusChanged.setId(1);
        claimStatusChanged.setChangedAt(OCCURED_AT);
        claimStatusChanged.setRevision(1);
        return claimStatusChanged;
    }

    public static ValidationResult testValidationResult() {
        PersonalDataValidationResult personalDataValidationResult = new PersonalDataValidationResult();
        personalDataValidationResult.setValidationId(randomString());
        personalDataValidationResult.setToken(randomString());
        personalDataValidationResult.setValidationStatus(ValidationStatus.valid);
        ValidationResult result = new ValidationResult();
        result.setPersonal(personalDataValidationResult);
        return result;
    }

    public static Chargeback createChargeback(String id) {
        Chargeback chargeback = new Chargeback();
        chargeback.setSequenceId(1L);
        chargeback.setChangeId(3);
        chargeback.setDomainRevision(1L);
        chargeback.setPartyRevision(2L);
        chargeback.setChargebackId(id);
        chargeback.setPaymentId("testPaymentId");
        chargeback.setInvoiceId(randomString());
        chargeback.setShopId("shopId");
        chargeback.setPartyId("partyId");
        chargeback.setExternalId("id");
        chargeback.setEventCreatedAt(LocalDateTime.now());
        chargeback.setCreatedAt(LocalDateTime.now());
        chargeback.setStatus(ChargebackStatus.pending);
        chargeback.setStage(ChargebackStage.chargeback);
        chargeback.setReasonCategory(ChargebackCategory.dispute);
        chargeback.setCurrent(true);
        chargeback.setWtime(LocalDateTime.now());
        return chargeback;
    }


    public static List<FinalCashFlowPosting> buildCashFlowPostings() {
        FinalCashFlowPosting external =
                new FinalCashFlowPosting();
        external.setDestination(
                new FinalCashFlowAccount()
                        .setAccountId(1)
                        .setAccountType(CashFlowAccount.external(
                                ExternalCashFlowAccount.income)));
        external.setSource(new FinalCashFlowAccount()
                .setAccountId(2)
                .setAccountType(CashFlowAccount.system(
                        SystemCashFlowAccount.settlement)));
        external.setVolume(new Cash()
                .setAmount(100L)
                .setCurrency(new CurrencyRef("RUB")));
        FinalCashFlowPosting provider =
                new FinalCashFlowPosting();
        provider.setDestination(
                new FinalCashFlowAccount()
                        .setAccountId(3)
                        .setAccountType(CashFlowAccount.provider(
                                ProviderCashFlowAccount.settlement)));
        provider.setSource(new FinalCashFlowAccount()
                .setAccountId(4)
                .setAccountType(CashFlowAccount.system(
                        SystemCashFlowAccount.settlement)));
        provider.setVolume(new Cash()
                .setAmount(100L)
                .setCurrency(new CurrencyRef("RUB")));
        FinalCashFlowPosting merchant =
                new FinalCashFlowPosting();
        merchant.setDestination(
                new FinalCashFlowAccount()
                        .setAccountId(5)
                        .setAccountType(CashFlowAccount.system(
                                SystemCashFlowAccount.settlement)));
        merchant.setSource(new FinalCashFlowAccount()
                .setAccountId(6)
                .setAccountType(CashFlowAccount.merchant(
                        MerchantCashFlowAccount.settlement)));
        merchant.setVolume(new Cash()
                .setAmount(200L)
                .setCurrency(new CurrencyRef("RUB")));
        return List.of(
                external,
                provider,
                merchant);
    }
}
