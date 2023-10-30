package dev.vality.daway.service;

import dev.vality.daway.config.PostgresqlSpringBootITest;
import dev.vality.daway.dao.invoicing.iface.CashFlowDao;
import dev.vality.daway.dao.invoicing.iface.CashFlowLinkDao;
import dev.vality.daway.domain.enums.PaymentChangeType;
import dev.vality.daway.domain.tables.pojos.CashFlow;
import dev.vality.daway.domain.tables.pojos.CashFlowLink;
import dev.vality.daway.handler.wrapper.payment.CashFlowWrapperHandler;
import dev.vality.daway.model.CashFlowWrapper;
import dev.vality.daway.model.PaymentWrapper;
import dev.vality.daway.utils.MockUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static dev.vality.daway.utils.JdbcUtil.countEntities;
import static dev.vality.daway.utils.JdbcUtil.countPaymentEntity;
import static org.junit.jupiter.api.Assertions.*;

@PostgresqlSpringBootITest
public class CashFlowWrapperHandlerTest {

    @Autowired
    private CashFlowWrapperHandler service;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CashFlowDao cashFlowDao;

    @Autowired
    private CashFlowLinkDao cashFlowLinkDao;

    private final String invoiceId = "invoiceId";
    private final String paymentId = "paymentId";

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("truncate table dw.cash_flow_link cascade");
        jdbcTemplate.execute("truncate table dw.cash_flow cascade");
    }

    @Test
    void saveTest() {
        CashFlowWrapper first = MockUtils.buildCashFlowWrapper(invoiceId, paymentId, 1L, 1);
        List<PaymentWrapper> wrappers = createPaymentWrappers(first);

        service.saveBatch(wrappers);
        CashFlowLink cashFlowLink = cashFlowLinkDao.get(invoiceId, paymentId);
        assertCashFlowLink(first.getCashFlowLink(), cashFlowLink);

        Map<Long, CashFlow> actualCashFlowsMap = cashFlowDao.getByObjId(cashFlowLink.getId(), PaymentChangeType.payment)
                .stream()
                .collect(Collectors.toMap(CashFlow::getAmount, cashFlow -> cashFlow));
        assertCashFlows(first.getCashFlows(), actualCashFlowsMap, cashFlowLink.getId());
    }

    @Test
    public void duplicationTest() {
        CashFlowWrapper first = MockUtils.buildCashFlowWrapper(invoiceId, paymentId, 1L, 1);
        CashFlowWrapper second = MockUtils.buildCashFlowWrapper(invoiceId, paymentId, 2L, 1);
        List<PaymentWrapper> wrappers = createPaymentWrappers(first, second);

        service.saveBatch(wrappers);
        CashFlowLink currentLink = cashFlowLinkDao.get(invoiceId, paymentId);
        assertCashFlowLink(second.getCashFlowLink(), currentLink);
        Map<Long, CashFlow> actualCashFlowsMap = cashFlowDao.getByObjId(currentLink.getId(), PaymentChangeType.payment)
                .stream()
                .collect(Collectors.toMap(CashFlow::getAmount, cashFlow -> cashFlow));
        assertCashFlows(second.getCashFlows(), actualCashFlowsMap, currentLink.getId());

        service.saveBatch(wrappers);
        assertEquals(2, countPaymentEntity(jdbcTemplate, "cash_flow_link", invoiceId, paymentId, false));
        assertEquals(1, countPaymentEntity(jdbcTemplate, "cash_flow_link", invoiceId, paymentId, true));
        assertEquals(6, countEntities(jdbcTemplate, "cash_flow"));
    }

    private void assertCashFlowLink(CashFlowLink expected, CashFlowLink actual) {
        assertNotNull(actual.getId());
        assertNotNull(actual.getWtime());
        assertTrue(actual.getCurrent());
        assertEquals(expected.getEventCreatedAt(), actual.getEventCreatedAt());
        assertEquals(expected.getInvoiceId(), actual.getInvoiceId());
        assertEquals(expected.getPaymentId(), actual.getPaymentId());
        assertEquals(expected.getSequenceId(), actual.getSequenceId());
        assertEquals(expected.getChangeId(), actual.getChangeId());
    }

    private void assertCashFlows(List<CashFlow> expectedCashFlows,
                                 Map<Long, CashFlow> actualCashFlowsByAmount,
                                 Long objId) {
        assertEquals(expectedCashFlows.size(), actualCashFlowsByAmount.size());
        expectedCashFlows.forEach(expected -> {
            CashFlow actual = actualCashFlowsByAmount.get(expected.getAmount());
            assertNotNull(actual.getId());
            assertNull(actual.getAdjFlowType());
            assertEquals(objId, actual.getObjId());
            assertEquals(expected.getObjType(), actual.getObjType());
            assertEquals(expected.getSourceAccountType(), actual.getSourceAccountType());
            assertEquals(expected.getSourceAccountTypeValue(), actual.getSourceAccountTypeValue());
            assertEquals(expected.getSourceAccountId(), actual.getSourceAccountId());
            assertEquals(expected.getDestinationAccountType(), actual.getDestinationAccountType());
            assertEquals(expected.getDestinationAccountTypeValue(), actual.getDestinationAccountTypeValue());
            assertEquals(expected.getDestinationAccountId(), actual.getDestinationAccountId());
            assertEquals(expected.getAmount(), actual.getAmount());
            assertEquals(expected.getCurrencyCode(), actual.getCurrencyCode());
            assertEquals(expected.getDetails(), actual.getDetails());
        });
    }

    private List<PaymentWrapper> createPaymentWrappers(CashFlowWrapper... wrappers) {
        List<PaymentWrapper> paymentWrappers = new ArrayList<>();
        for (CashFlowWrapper cashFlowWrapper : wrappers) {
            var paymentWrapper = new PaymentWrapper();
            paymentWrapper.setCashFlowWrapper(cashFlowWrapper);
            paymentWrappers.add(paymentWrapper);
        }
        return paymentWrappers;
    }


}
