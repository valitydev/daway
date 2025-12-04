package dev.vality.daway.dao;

import dev.vality.daway.config.PostgresqlSpringBootITest;
import dev.vality.daway.dao.dominant.iface.DominantDao;
import dev.vality.daway.dao.dominant.impl.*;
import dev.vality.daway.dao.invoicing.iface.*;
import dev.vality.daway.dao.invoicing.impl.CashFlowLinkIdsGeneratorDaoImpl;
import dev.vality.daway.dao.invoicing.impl.InvoiceDaoImpl;
import dev.vality.daway.dao.invoicing.impl.PaymentDaoImpl;
import dev.vality.daway.dao.dominant.impl.PartyDaoImpl;
import dev.vality.daway.dao.dominant.impl.ShopDaoImpl;
import dev.vality.daway.dao.rate.iface.RateDao;
import dev.vality.daway.domain.enums.CashFlowAccount;
import dev.vality.daway.domain.enums.PaymentChangeType;
import dev.vality.daway.domain.tables.pojos.*;
import dev.vality.daway.domain.tables.pojos.Calendar;
import dev.vality.daway.domain.tables.pojos.Currency;
import dev.vality.daway.exception.NotFoundException;
import dev.vality.daway.model.InvoicingKey;
import dev.vality.daway.utils.HashUtil;
import dev.vality.testcontainers.annotations.util.RandomBeans;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;
import java.util.stream.LongStream;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
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
    private PartyDaoImpl partyDao;
    @Autowired
    private ShopDaoImpl shopDao;
    @Autowired
    private RateDao rateDao;
    @Autowired
    private CashFlowLinkIdsGeneratorDaoImpl idsGeneratorDao;


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
    void invoiceCartDaoTest() {
        jdbcTemplate.execute("truncate table dw.invoice_cart cascade");
        String invoiceId = UUID.randomUUID().toString();
        List<InvoiceCart> invoiceCarts = RandomBeans.randomListOf(10, InvoiceCart.class);
        invoiceCarts.forEach(ic -> ic.setInvoiceId(invoiceId));
        invoiceCartDao.save(invoiceCarts);
        assertEquals(invoiceCarts, invoiceCartDao.getByInvoiceId(invoiceId));
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
    void partyDaoTest() {
        jdbcTemplate.execute("truncate table dw.party cascade");
        Party party = RandomBeans.random(Party.class);
        party.setCurrent(true);
        partyDao.save(party);
        Party partyGet = partyDao.get(party.getPartyId());
        assertEquals(party, partyGet);
        String oldId = party.getPartyId();

        Integer changeId = party.getChangeId() + 1;
        party.setChangeId(changeId);
        party.setId(party.getId() + 1);
        partyDao.updateNotCurrent(oldId);
        partyDao.save(party);

        partyGet = partyDao.get(oldId);
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
        String id = shop.getShopId();
        shop.setId(shop.getId() + 1);
        shopDao.updateNotCurrent(id);
        shopDao.save(shop);
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
                new BeanPropertyRowMapper(Rate.class),
                new Object[]{id}
        ));

        List<Long> ids = rateDao.getIds(rate.getSourceId());
        Assertions.assertNotNull(ids);
        Assertions.assertFalse(ids.isEmpty());
        assertEquals(1, ids.size());
        assertEquals(id, ids.get(0));

        rateDao.updateNotCurrent(Collections.singletonList(id));
        assertThrows(EmptyResultDataAccessException.class, () -> jdbcTemplate.queryForObject(
                "SELECT * FROM dw.rate AS rate WHERE rate.id = ? AND rate.current",
                new BeanPropertyRowMapper(Rate.class),
                new Object[]{id}
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
}
