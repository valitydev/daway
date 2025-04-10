package dev.vality.daway.dao;

import dev.vality.daway.config.PostgresqlSpringBootITest;
import dev.vality.daway.dao.dominant.iface.DominantDao;
import dev.vality.daway.dao.dominant.impl.*;
import dev.vality.daway.dao.invoicing.iface.*;
import dev.vality.daway.dao.invoicing.impl.CashFlowLinkIdsGeneratorDaoImpl;
import dev.vality.daway.dao.invoicing.impl.InvoiceDaoImpl;
import dev.vality.daway.dao.invoicing.impl.PaymentDaoImpl;
import dev.vality.daway.dao.party.iface.*;
import dev.vality.daway.dao.rate.iface.RateDao;
import dev.vality.daway.dao.recurrent.payment.tool.iface.RecurrentPaymentToolDao;
import dev.vality.daway.domain.enums.CashFlowAccount;
import dev.vality.daway.domain.enums.PaymentChangeType;
import dev.vality.daway.domain.tables.pojos.Calendar;
import dev.vality.daway.domain.tables.pojos.Currency;
import dev.vality.daway.domain.tables.pojos.*;
import dev.vality.daway.exception.NotFoundException;
import dev.vality.daway.model.InvoicingKey;
import dev.vality.daway.utils.HashUtil;
import dev.vality.testcontainers.annotations.util.RandomBeans;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.LongStream;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@PostgresqlSpringBootITest
class DaoTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private CalendarDaoImpl calendarDao;
    @Autowired
    private CategoryDaoImpl categoryDao;
    @Autowired
    private CurrencyDaoImpl currencyDao;
    @Autowired
    private InspectorDaoImpl inspectorDao;
    @Autowired
    private PaymentInstitutionDaoImpl paymentInstitutionDao;
    @Autowired
    private PaymentMethodDaoImpl paymentMethodDao;
    @Autowired
    private ProviderDaoImpl providerDao;
    @Autowired
    private WithdrawalProviderDaoImpl withdrawalProviderDao;
    @Autowired
    private ProxyDaoImpl proxyDao;
    @Autowired
    private TerminalDaoImpl terminalDao;
    @Autowired
    private TermSetHierarchyDaoImpl termSetHierarchyDao;
    @Autowired
    private DominantDao dominantDao;
    @Autowired
    private CashFlowLinkDao cashFlowLinkDao;
    @Autowired
    private CashFlowDao cashFlowDao;
    @Autowired
    private AdjustmentDao adjustmentDao;
    @Autowired
    private PaymentDaoImpl paymentDao;
    @Autowired
    private PaymentStatusInfoDao paymentStatusInfoDao;
    @Autowired
    private PaymentPayerInfoDao paymentPayerInfoDao;
    @Autowired
    private PaymentAdditionalInfoDao paymentAdditionalInfoDao;
    @Autowired
    private PaymentRecurrentInfoDao paymentRecurrentInfoDao;
    @Autowired
    private PaymentRiskDataDao paymentRiskDataDao;
    @Autowired
    private PaymentFeeDao paymentFeeDao;
    @Autowired
    private PaymentRouteDao paymentRouteDao;
    @Autowired
    private InvoiceCartDao invoiceCartDao;
    @Autowired
    private InvoiceDaoImpl invoiceDao;
    @Autowired
    private InvoiceStatusInfoDao invoiceStatusInfoDao;
    @Autowired
    private RefundDao refundDao;
    @Autowired
    private ContractAdjustmentDao contractAdjustmentDao;
    @Autowired
    private ContractDao contractDao;
    @Autowired
    private ContractorDao contractorDao;
    @Autowired
    private PartyDao partyDao;
    @Autowired
    private ShopDao shopDao;
    @Autowired
    private RateDao rateDao;
    @Autowired
    private CashFlowLinkIdsGeneratorDaoImpl idsGeneratorDao;
    @Autowired
    private RecurrentPaymentToolDao recurrentPaymentToolDao;


    @Test
    void dominantDaoTest() {
        jdbcTemplate.execute("truncate table dw.calendar cascade");
        jdbcTemplate.execute("truncate table dw.category cascade");
        jdbcTemplate.execute("truncate table dw.currency cascade");
        jdbcTemplate.execute("truncate table dw.inspector cascade");
        jdbcTemplate.execute("truncate table dw.payment_institution cascade");
        jdbcTemplate.execute("truncate table dw.payment_method cascade");
        jdbcTemplate.execute("truncate table dw.provider cascade");
        jdbcTemplate.execute("truncate table dw.withdrawal_provider cascade");
        jdbcTemplate.execute("truncate table dw.proxy cascade");
        jdbcTemplate.execute("truncate table dw.terminal cascade");
        jdbcTemplate.execute("truncate table dw.term_set_hierarchy cascade");

        var calendar = RandomBeans.random(Calendar.class);
        calendar.setCurrent(true);
        calendarDao.save(calendar);
        calendarDao.updateNotCurrent(calendar.getCalendarRefId());

        Category category = RandomBeans.random(Category.class);
        category.setCurrent(true);
        categoryDao.save(category);
        categoryDao.updateNotCurrent(category.getCategoryRefId());

        var currency = RandomBeans.random(Currency.class);
        currency.setCurrent(true);
        currencyDao.save(currency);
        currencyDao.updateNotCurrent(currency.getCurrencyRefId());

        Inspector inspector = RandomBeans.random(Inspector.class);
        inspector.setCurrent(true);
        inspectorDao.save(inspector);
        inspectorDao.updateNotCurrent(inspector.getInspectorRefId());

        PaymentInstitution paymentInstitution = RandomBeans.random(PaymentInstitution.class);
        paymentInstitution.setCurrent(true);
        paymentInstitutionDao.save(paymentInstitution);
        paymentInstitutionDao.updateNotCurrent(paymentInstitution.getPaymentInstitutionRefId());

        PaymentMethod paymentMethod = RandomBeans.random(PaymentMethod.class);
        paymentMethod.setCurrent(true);
        paymentMethodDao.save(paymentMethod);
        paymentMethodDao.updateNotCurrent(paymentMethod.getPaymentMethodRefId());

        Provider provider = RandomBeans.random(Provider.class);
        provider.setCurrent(true);
        providerDao.save(provider);
        providerDao.updateNotCurrent(provider.getProviderRefId());

        WithdrawalProvider withdrawalProvider = RandomBeans.random(WithdrawalProvider.class);
        withdrawalProvider.setCurrent(true);
        withdrawalProviderDao.save(withdrawalProvider);
        withdrawalProviderDao.updateNotCurrent(withdrawalProvider.getWithdrawalProviderRefId());

        Proxy proxy = RandomBeans.random(Proxy.class);
        proxy.setCurrent(true);
        proxyDao.save(proxy);
        proxyDao.updateNotCurrent(proxy.getProxyRefId());

        Terminal terminal = RandomBeans.random(Terminal.class);
        terminal.setCurrent(true);
        terminalDao.save(terminal);
        terminalDao.updateNotCurrent(terminal.getTerminalRefId());

        TermSetHierarchy termSetHierarchy = RandomBeans.random(TermSetHierarchy.class);
        termSetHierarchy.setCurrent(true);
        termSetHierarchyDao.save(termSetHierarchy);
        termSetHierarchyDao.updateNotCurrent(termSetHierarchy.getTermSetHierarchyRefId());

        OptionalLong maxVersionId = LongStream.of(
                calendar.getVersionId(),
                category.getVersionId(),
                currency.getVersionId(),
                inspector.getVersionId(),
                paymentInstitution.getVersionId(),
                paymentMethod.getVersionId(),
                provider.getVersionId(),
                withdrawalProvider.getVersionId(),
                proxy.getVersionId(),
                terminal.getVersionId(),
                termSetHierarchy.getVersionId()).max();

        dominantDao.updateLastVersionId(maxVersionId.getAsLong());
        Long lastVersionId = dominantDao.getLastVersionId();

        assertEquals(maxVersionId.getAsLong(), lastVersionId.longValue());
    }

    @Test
    void cashFlowDaoTest() {
        jdbcTemplate.execute("truncate table dw.cash_flow_link cascade");
        jdbcTemplate.execute("truncate table dw.cash_flow cascade");
        Long cashFlowLink = 123L;
        List<CashFlow> cashFlowList = RandomBeans.randomListOf(100, CashFlow.class);
        cashFlowList.forEach(cf -> {
            cf.setObjId(cashFlowLink);
            cf.setAmount((long) new Random().nextInt(100));
            cf.setObjType(PaymentChangeType.payment);
            cf.setAdjFlowType(null);
            cf.setSourceAccountTypeValue("settlement");
            if (cf.getDestinationAccountType() == CashFlowAccount.external) {
                cf.setDestinationAccountTypeValue("income");
            } else {
                cf.setDestinationAccountTypeValue("settlement");
            }
        });
        cashFlowDao.save(cashFlowList);
        List<CashFlow> byObjId = cashFlowDao.getByObjId(cashFlowLink, PaymentChangeType.payment);
        assertEquals(new HashSet(byObjId), new HashSet(cashFlowList));
    }

    @Test
    void cashFlowDaoLinkTest() {
        jdbcTemplate.execute("truncate table dw.cash_flow_link cascade");
        List<CashFlowLink> cashFlowLinks = RandomBeans.randomListOf(2, CashFlowLink.class);
        for (int i = 0; i < cashFlowLinks.size(); i++) {
            cashFlowLinks.get(i).setId(i + 1L);
            cashFlowLinks.get(i).setCurrent(true);
        }
        cashFlowLinkDao.saveBatch(cashFlowLinks);
        CashFlowLink first = cashFlowLinks.get(0);
        assertEquals(first, cashFlowLinkDao.get(first.getInvoiceId(), first.getPaymentId()));
        CashFlowLink second = cashFlowLinks.get(1);
        assertEquals(second, cashFlowLinkDao.get(second.getInvoiceId(), second.getPaymentId()));

        CashFlowLink third = RandomBeans.random(CashFlowLink.class, "id", "current", "invoiceId", "paymentId");
        third.setId(3L);
        third.setCurrent(false);
        third.setInvoiceId(first.getInvoiceId());
        third.setPaymentId(first.getPaymentId());
        cashFlowLinkDao.saveBatch(List.of(third));
        assertEquals(first, cashFlowLinkDao.get(third.getInvoiceId(), third.getPaymentId()));
        cashFlowLinkDao.switchCurrent(Set.of(InvoicingKey.buildKey(third.getInvoiceId(), third.getPaymentId())));
        third.setCurrent(true);
        assertEquals(third, cashFlowLinkDao.get(third.getInvoiceId(), third.getPaymentId()));
    }


    @Test
    void adjustmentDaoTest() {
        jdbcTemplate.execute("truncate table dw.adjustment cascade");
        Adjustment adjustment = RandomBeans.random(Adjustment.class);
        adjustment.setCurrent(true);
        adjustmentDao.save(adjustment);
        assertEquals(adjustment.getPartyId(), adjustmentDao.get(adjustment.getInvoiceId(), adjustment.getPaymentId(), adjustment.getAdjustmentId())
                .getPartyId());
        adjustmentDao.updateNotCurrent(adjustment.getId());

        assertThrows(NotFoundException.class, () -> adjustmentDao.get(adjustment.getInvoiceId(), adjustment.getPaymentId(), adjustment.getAdjustmentId()));
    }

    @Test
    void invoiceDaoTest() {
        jdbcTemplate.execute("truncate table dw.invoice cascade");
        List<Invoice> invoices = RandomBeans.randomListOf(3, Invoice.class);
        invoiceDao.saveBatch(invoices);
        assertEquals(invoices.get(0), invoiceDao.get(invoices.get(0).getInvoiceId()));
        assertEquals(invoices.get(1), invoiceDao.get(invoices.get(1).getInvoiceId()));
        assertEquals(invoices.get(2), invoiceDao.get(invoices.get(2).getInvoiceId()));
    }


    @Test
    void invoiceStatusInfoDaoTest() {
        jdbcTemplate.execute("truncate table dw.invoice_status_info cascade");
        List<InvoiceStatusInfo> statusInfos =
                RandomBeans.randomListOf(3, InvoiceStatusInfo.class);
        statusInfos.forEach(status -> status.setCurrent(true));
        invoiceStatusInfoDao.saveBatch(statusInfos);
        assertEquals(statusInfos.get(0), invoiceStatusInfoDao.get(statusInfos.get(0).getInvoiceId()));
        assertEquals(statusInfos.get(1), invoiceStatusInfoDao.get(statusInfos.get(1).getInvoiceId()));
        assertEquals(statusInfos.get(2), invoiceStatusInfoDao.get(statusInfos.get(2).getInvoiceId()));

        InvoiceStatusInfo statusInfo = RandomBeans.random(InvoiceStatusInfo.class);
        InvoiceStatusInfo initialStatusInfo = statusInfos.get(0);
        statusInfo.setInvoiceId(initialStatusInfo.getInvoiceId());
        statusInfo.setCurrent(false);
        statusInfo.setId(initialStatusInfo.getId() + 1);
        invoiceStatusInfoDao.saveBatch(List.of(statusInfo));
        assertNotEquals(statusInfo, initialStatusInfo);
        assertEquals(initialStatusInfo, invoiceStatusInfoDao.get(initialStatusInfo.getInvoiceId()));
        invoiceStatusInfoDao.switchCurrent(Set.of(statusInfo.getInvoiceId()));
        statusInfo.setCurrent(true);
        assertEquals(statusInfo, invoiceStatusInfoDao.get(initialStatusInfo.getInvoiceId()));
    }

    @Test
    void invoiceCartDaoTest() {
        jdbcTemplate.execute("truncate table dw.invoice_cart cascade");
        String invoiceId = UUID.randomUUID().toString();
        List<InvoiceCart> invoiceCarts = RandomBeans.randomListOf(10, InvoiceCart.class);
        invoiceCarts.forEach(ic -> ic.setInvoiceId(invoiceId));
        invoiceCartDao.save(invoiceCarts);
        assertEquals(invoiceCarts, invoiceCartDao.getByInvoiceId(invoiceId));
    }

    @Test
    void paymentDaoTest() {
        jdbcTemplate.execute("truncate table dw.payment cascade");
        Payment first = RandomBeans.random(Payment.class);
        first.setId(1L);
        first.setEventCreatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        Payment second = RandomBeans.random(Payment.class);
        second.setId(2L);
        second.setEventCreatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        paymentDao.saveBatch(Arrays.asList(first, second));
        assertEquals(first, paymentDao.get(first.getInvoiceId(), first.getPaymentId()));
        assertEquals(second, paymentDao.get(second.getInvoiceId(), second.getPaymentId()));
    }

    @Test
    void paymentStatusInfoDaoTest() {
        jdbcTemplate.execute("truncate table dw.payment_status_info cascade");
        List<PaymentStatusInfo> statusInfos = RandomBeans.randomListOf(2, PaymentStatusInfo.class);
        statusInfos.forEach(statusInfo -> statusInfo.setCurrent(true));
        paymentStatusInfoDao.saveBatch(statusInfos);
        PaymentStatusInfo first = statusInfos.get(0);
        assertEquals(first, paymentStatusInfoDao.get(first.getInvoiceId(), first.getPaymentId()));
        PaymentStatusInfo second = statusInfos.get(1);
        assertEquals(second, paymentStatusInfoDao.get(second.getInvoiceId(), second.getPaymentId()));

        PaymentStatusInfo third = RandomBeans.random(PaymentStatusInfo.class);
        third.setId(first.getId() + 1);
        third.setCurrent(false);
        third.setInvoiceId(first.getInvoiceId());
        third.setPaymentId(first.getPaymentId());
        paymentStatusInfoDao.saveBatch(List.of(third));
        assertEquals(first, paymentStatusInfoDao.get(third.getInvoiceId(), third.getPaymentId()));
        paymentStatusInfoDao.switchCurrent(Set.of(InvoicingKey.buildKey(third.getInvoiceId(), third.getPaymentId())));
        third.setCurrent(true);
        assertEquals(third, paymentStatusInfoDao.get(third.getInvoiceId(), third.getPaymentId()));
    }

    @Test
    void paymentPayerInfoDaoTest() {
        jdbcTemplate.execute("truncate table dw.payment_payer_info cascade");
        PaymentPayerInfo first = RandomBeans.random(PaymentPayerInfo.class);
        first.setId(1L);
        PaymentPayerInfo second = RandomBeans.random(PaymentPayerInfo.class);
        second.setId(2L);
        paymentPayerInfoDao.saveBatch(Arrays.asList(first, second));
        assertEquals(first, paymentPayerInfoDao.get(first.getInvoiceId(), first.getPaymentId()));
        assertEquals(second, paymentPayerInfoDao.get(second.getInvoiceId(), second.getPaymentId()));
    }

    @Test
    void paymentAdditionalInfoDaoTest() {
        jdbcTemplate.execute("truncate table dw.payment_additional_info cascade");
        List<PaymentAdditionalInfo> list = RandomBeans.randomListOf(2, PaymentAdditionalInfo.class);
        list.forEach(statusInfo -> statusInfo.setCurrent(true));
        paymentAdditionalInfoDao.saveBatch(list);
        PaymentAdditionalInfo first = list.get(0);
        assertEquals(first, paymentAdditionalInfoDao.get(first.getInvoiceId(), first.getPaymentId()));
        PaymentAdditionalInfo second = list.get(1);
        assertEquals(second, paymentAdditionalInfoDao.get(second.getInvoiceId(), second.getPaymentId()));

        PaymentAdditionalInfo third = RandomBeans.random(PaymentAdditionalInfo.class);
        third.setId(first.getId() + 1);
        third.setCurrent(false);
        third.setInvoiceId(first.getInvoiceId());
        third.setPaymentId(first.getPaymentId());
        paymentAdditionalInfoDao.saveBatch(List.of(third));
        assertEquals(first, paymentAdditionalInfoDao.get(third.getInvoiceId(), third.getPaymentId()));
        paymentAdditionalInfoDao.switchCurrent(Set.of(InvoicingKey.buildKey(third.getInvoiceId(), third.getPaymentId())));
        third.setCurrent(true);
        assertEquals(third, paymentAdditionalInfoDao.get(third.getInvoiceId(), third.getPaymentId()));
    }

    @Test
    void paymentRecurrentInfoDaoTest() {
        jdbcTemplate.execute("truncate table dw.payment_recurrent_info cascade");
        List<PaymentRecurrentInfo> list = RandomBeans.randomListOf(2, PaymentRecurrentInfo.class);
        list.forEach(statusInfo -> statusInfo.setCurrent(true));
        paymentRecurrentInfoDao.saveBatch(list);
        PaymentRecurrentInfo first = list.get(0);
        assertEquals(first, paymentRecurrentInfoDao.get(first.getInvoiceId(), first.getPaymentId()));
        PaymentRecurrentInfo second = list.get(1);
        assertEquals(second, paymentRecurrentInfoDao.get(second.getInvoiceId(), second.getPaymentId()));

        PaymentRecurrentInfo third = RandomBeans.random(PaymentRecurrentInfo.class);
        third.setId(first.getId() + 1);
        third.setCurrent(false);
        third.setInvoiceId(first.getInvoiceId());
        third.setPaymentId(first.getPaymentId());
        paymentRecurrentInfoDao.saveBatch(List.of(third));
        assertEquals(first, paymentRecurrentInfoDao.get(third.getInvoiceId(), third.getPaymentId()));
        paymentRecurrentInfoDao.switchCurrent(Set.of(InvoicingKey.buildKey(third.getInvoiceId(), third.getPaymentId())));
        third.setCurrent(true);
        assertEquals(third, paymentRecurrentInfoDao.get(third.getInvoiceId(), third.getPaymentId()));
    }

    @Test
    void paymentRiskDataDaoTest() {
        jdbcTemplate.execute("truncate table dw.payment_risk_data cascade");
        List<PaymentRiskData> list = RandomBeans.randomListOf(2, PaymentRiskData.class);
        list.forEach(statusInfo -> statusInfo.setCurrent(true));
        paymentRiskDataDao.saveBatch(list);
        PaymentRiskData first = list.get(0);
        assertEquals(first, paymentRiskDataDao.get(first.getInvoiceId(), first.getPaymentId()));
        PaymentRiskData second = list.get(1);
        assertEquals(second, paymentRiskDataDao.get(second.getInvoiceId(), second.getPaymentId()));

        PaymentRiskData third = RandomBeans.random(PaymentRiskData.class);
        third.setId(first.getId() + 1);
        third.setCurrent(false);
        third.setInvoiceId(first.getInvoiceId());
        third.setPaymentId(first.getPaymentId());
        paymentRiskDataDao.saveBatch(List.of(third));
        assertEquals(first, paymentRiskDataDao.get(third.getInvoiceId(), third.getPaymentId()));
        paymentRiskDataDao.switchCurrent(Set.of(InvoicingKey.buildKey(third.getInvoiceId(), third.getPaymentId())));
        third.setCurrent(true);
        assertEquals(third, paymentRiskDataDao.get(third.getInvoiceId(), third.getPaymentId()));
    }

    @Test
    void paymentFeeDaoTest() {
        jdbcTemplate.execute("truncate table dw.payment_fee cascade");
        List<PaymentFee> list = RandomBeans.randomListOf(2, PaymentFee.class);
        list.forEach(statusInfo -> statusInfo.setCurrent(true));
        paymentFeeDao.saveBatch(list);
        PaymentFee first = list.get(0);
        assertEquals(first, paymentFeeDao.get(first.getInvoiceId(), first.getPaymentId()));
        PaymentFee second = list.get(1);
        assertEquals(second, paymentFeeDao.get(second.getInvoiceId(), second.getPaymentId()));

        PaymentFee third = RandomBeans.random(PaymentFee.class);
        third.setId(first.getId() + 1);
        third.setCurrent(false);
        third.setInvoiceId(first.getInvoiceId());
        third.setPaymentId(first.getPaymentId());
        paymentFeeDao.saveBatch(List.of(third));
        assertEquals(first, paymentFeeDao.get(third.getInvoiceId(), third.getPaymentId()));
        paymentFeeDao.switchCurrent(Set.of(InvoicingKey.buildKey(third.getInvoiceId(), third.getPaymentId())));
        third.setCurrent(true);
        assertEquals(third, paymentFeeDao.get(third.getInvoiceId(), third.getPaymentId()));
    }

    @Test
    void paymentRouteDaoTest() {
        jdbcTemplate.execute("truncate table dw.payment_route cascade");
        List<PaymentRoute> list = RandomBeans.randomListOf(2, PaymentRoute.class);
        list.forEach(statusInfo -> statusInfo.setCurrent(true));
        paymentRouteDao.saveBatch(list);
        PaymentRoute first = list.get(0);
        assertEquals(first, paymentRouteDao.get(first.getInvoiceId(), first.getPaymentId()));
        PaymentRoute second = list.get(1);
        assertEquals(second, paymentRouteDao.get(second.getInvoiceId(), second.getPaymentId()));

        PaymentRoute third = RandomBeans.random(PaymentRoute.class);
        third.setId(first.getId() + 1);
        third.setCurrent(false);
        third.setInvoiceId(first.getInvoiceId());
        third.setPaymentId(first.getPaymentId());
        paymentRouteDao.saveBatch(List.of(third));
        assertEquals(first, paymentRouteDao.get(third.getInvoiceId(), third.getPaymentId()));
        paymentRouteDao.switchCurrent(Set.of(InvoicingKey.buildKey(third.getInvoiceId(), third.getPaymentId())));
        third.setCurrent(true);
        assertEquals(third, paymentRouteDao.get(third.getInvoiceId(), third.getPaymentId()));
    }

    @Test
    void refundDaoTest() {
        jdbcTemplate.execute("truncate table dw.refund cascade");
        Refund refund = RandomBeans.random(Refund.class);
        refund.setCurrent(true);
        refundDao.save(refund);
        Refund refundGet = refundDao.get(refund.getInvoiceId(), refund.getPaymentId(), refund.getRefundId());
        assertEquals(refund, refundGet);
        refundDao.updateNotCurrent(refund.getId());

        refundDao.updateCommissions(refund.getId());

        assertThrows(NotFoundException.class, () -> refundDao.get(refund.getInvoiceId(), refund.getPaymentId(), refund.getRefundId()));
    }

    @Test
    void contractAdjustmentDaoTest() {
        jdbcTemplate.execute("truncate table dw.contract_adjustment cascade");
        jdbcTemplate.execute("truncate table dw.contract cascade");
        Contract contract = RandomBeans.random(Contract.class);
        contract.setCurrent(true);
        Long cntrctId = contractDao.save(contract).get();
        List<ContractAdjustment> contractAdjustments = RandomBeans.randomListOf(10, ContractAdjustment.class);
        contractAdjustments.forEach(ca -> ca.setCntrctId(cntrctId));
        contractAdjustmentDao.save(contractAdjustments);
        List<ContractAdjustment> byCntrctId = contractAdjustmentDao.getByCntrctId(cntrctId);
        assertEquals(new HashSet(contractAdjustments), new HashSet(byCntrctId));
    }

    @Test
    void contractDaoTest() {
        jdbcTemplate.execute("truncate table dw.contract cascade");
        Contract contract = RandomBeans.random(Contract.class);
        contract.setCurrent(true);
        contractDao.save(contract);
        Contract contractGet = contractDao.get(contract.getPartyId(), contract.getContractId());
        assertEquals(contract, contractGet);
    }

    @Test
    void contractorDaoTest() {
        jdbcTemplate.execute("truncate table dw.contractor cascade");
        Contractor contractor = RandomBeans.random(Contractor.class);
        contractor.setCurrent(true);
        contractorDao.save(contractor);
        Contractor contractorGet = contractorDao.get(contractor.getPartyId(), contractor.getContractorId());
        assertEquals(contractor, contractorGet);
        Integer changeId = contractor.getChangeId() + 1;
        contractor.setChangeId(changeId);
        Long oldId = contractor.getId();
        contractor.setId(contractor.getId() + 1);
        contractorDao.save(contractor);
        contractorDao.updateNotCurrent(oldId);
    }

    @Test
    void partyDaoTest() {
        jdbcTemplate.execute("truncate table dw.party cascade");
        Party party = RandomBeans.random(Party.class);
        party.setCurrent(true);
        partyDao.save(party);
        Party partyGet = partyDao.get(party.getPartyId());
        assertEquals(party, partyGet);
        Long oldId = party.getId();

        Integer changeId = party.getChangeId() + 1;
        party.setChangeId(changeId);
        party.setId(party.getId() + 1);
        partyDao.save(party);
        partyDao.updateNotCurrent(oldId);

        partyGet = partyDao.get(party.getPartyId());
        assertEquals(changeId, partyGet.getChangeId());
    }

    @Test
    void shopDaoTest() {
        jdbcTemplate.execute("truncate table dw.shop cascade");
        Shop shop = RandomBeans.random(Shop.class);
        shop.setCurrent(true);
        shopDao.save(shop);
        Shop shopGet = shopDao.get(shop.getPartyId(), shop.getShopId());
        assertEquals(shop, shopGet);

        Integer changeId = shop.getChangeId() + 1;
        shop.setChangeId(changeId);
        Long id = shop.getId();
        shop.setId(id + 1);
        shopDao.save(shop);
        shopDao.updateNotCurrent(id);
    }

    @Test
    void rateDaoTest() {
        jdbcTemplate.execute("truncate table dw.rate cascade");
        Rate rate = RandomBeans.random(Rate.class);
        rate.setCurrent(true);

        Long id = rateDao.save(rate);
        rate.setId(id);
        assertEquals(rate, jdbcTemplate.queryForObject(
                "SELECT * FROM dw.rate WHERE id = ? ",
                new Object[]{id},
                new BeanPropertyRowMapper(Rate.class)
        ));

        List<Long> ids = rateDao.getIds(rate.getSourceId());
        Assertions.assertNotNull(ids);
        Assertions.assertFalse(ids.isEmpty());
        assertEquals(1, ids.size());
        assertEquals(id, ids.get(0));

        rateDao.updateNotCurrent(Collections.singletonList(id));
        assertThrows(EmptyResultDataAccessException.class, () -> jdbcTemplate.queryForObject(
                "SELECT * FROM dw.rate AS rate WHERE rate.id = ? AND rate.current",
                new Object[]{id},
                new BeanPropertyRowMapper(Rate.class)
        ));
    }

    @Test
    void getIntHashTest() {
        Integer javaHash = HashUtil.getIntHash("kek");
        Integer postgresHash =
                jdbcTemplate.queryForObject("select ('x0'||substr(md5('kek'), 1, 7))::bit(32)::int", Integer.class);
        assertEquals(javaHash, postgresHash);
    }

    @Test
    void constraintTests() {
        jdbcTemplate.execute("truncate table dw.adjustment cascade");
        Adjustment adjustment = RandomBeans.random(Adjustment.class);
        adjustment.setChangeId(1);
        adjustment.setSequenceId(1L);
        adjustment.setInvoiceId("1");
        adjustment.setPartyId("1");
        adjustment.setCurrent(true);
        adjustmentDao.save(adjustment);

        assertEquals("1", adjustmentDao.get(adjustment.getInvoiceId(), adjustment.getPaymentId(), adjustment.getAdjustmentId())
                .getPartyId());

        adjustment.setPartyId("2");

        adjustmentDao.save(adjustment);

        assertEquals("1", adjustmentDao.get(adjustment.getInvoiceId(), adjustment.getPaymentId(), adjustment.getAdjustmentId())
                .getPartyId());
    }

    @Test
    void idsGeneratorTest() {
        List<Long> list = idsGeneratorDao.get(100);
        assertEquals(100, list.size());
        assertEquals(99, list.get(99) - list.get(0));
    }

    @Test
    void recurrentPaymentToolDaoTest() {
        jdbcTemplate.execute("truncate table dw.recurrent_payment_tool cascade");
        RecurrentPaymentTool recurrentPaymentTool = RandomBeans.random(RecurrentPaymentTool.class);
        recurrentPaymentTool.setCurrent(true);
        Optional<Long> id = recurrentPaymentToolDao.save(recurrentPaymentTool);
        Assertions.assertTrue(id.isPresent());
        recurrentPaymentTool.setId(id.get());
        assertEquals(recurrentPaymentTool, recurrentPaymentToolDao.get(recurrentPaymentTool.getRecurrentPaymentToolId()));
        recurrentPaymentToolDao.updateNotCurrent(recurrentPaymentTool.getId());

        assertThrows(NotFoundException.class, () -> recurrentPaymentToolDao.get(recurrentPaymentTool.getRecurrentPaymentToolId()));
    }
}
