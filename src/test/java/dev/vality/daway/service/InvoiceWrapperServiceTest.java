package dev.vality.daway.service;

import dev.vality.daway.config.PostgresqlSpringBootITest;
import dev.vality.daway.dao.invoicing.iface.InvoiceCartDao;
import dev.vality.daway.dao.invoicing.iface.InvoiceDao;
import dev.vality.daway.dao.invoicing.iface.InvoiceStatusInfoDao;
import dev.vality.daway.domain.tables.pojos.Invoice;
import dev.vality.daway.domain.tables.pojos.InvoiceCart;
import dev.vality.daway.domain.tables.pojos.InvoiceStatusInfo;
import dev.vality.daway.model.InvoiceWrapper;
import dev.vality.testcontainers.annotations.util.RandomBeans;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static dev.vality.daway.utils.JdbcUtil.countEntities;
import static dev.vality.daway.utils.JdbcUtil.countInvoiceEntity;
import static org.junit.jupiter.api.Assertions.*;

@PostgresqlSpringBootITest
public class InvoiceWrapperServiceTest {

    @Autowired
    private InvoiceWrapperService invoiceWrapperService;

    @Autowired
    private InvoiceDao invoiceDao;

    @Autowired
    private InvoiceStatusInfoDao invoiceStatusInfoDao;

    @Autowired
    private InvoiceCartDao invoiceCartDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void processTest() {
        List<InvoiceWrapper> invoiceWrappers = prepareInvoiceWrappers();
        invoiceWrapperService.save(invoiceWrappers);
        invoiceWrappers.forEach(this::assertInvoiceWrapper);
    }

    @Test
    public void duplicationTest() {
        List<InvoiceWrapper> invoiceWrappers = prepareInvoiceWrappers();
        invoiceWrapperService.save(invoiceWrappers);

        invoiceWrapperService.save(invoiceWrappers);
        invoiceWrappers.forEach(wrapper -> assertDuplication(wrapper.getInvoice().getInvoiceId()));
        assertTotal();
    }

    private List<InvoiceWrapper> prepareInvoiceWrappers() {
        List<InvoiceWrapper> invoiceWrappers = IntStream.range(1, 5)
                .mapToObj(x -> new InvoiceWrapper(
                        RandomBeans.random(Invoice.class, "id"),
                        RandomBeans.random(InvoiceStatusInfo.class, "id", "invoiceId"),
                        RandomBeans.randomListOf(3, InvoiceCart.class, "id", "invoiceId")))
                .collect(Collectors.toList());

        invoiceWrappers.forEach(iw -> {
            iw.getInvoiceStatusInfo().setInvoiceId(iw.getInvoice().getInvoiceId());
            iw.getCarts().forEach(cart ->
                    cart.setInvoiceId(iw.getInvoice().getInvoiceId()));
        });
        return invoiceWrappers;
    }

    private void assertInvoiceWrapper(InvoiceWrapper invoiceWrapper) {
        assertInvoice(invoiceWrapper.getInvoice());
        assertInvoiceStatus(invoiceWrapper.getInvoiceStatusInfo());
        assertInvoiceCart(invoiceWrapper.getCarts());
    }

    private void assertInvoice(Invoice expected) {
        Invoice actual = invoiceDao.get(expected.getInvoiceId());
        assertNotNull(actual.getId());
        actual.setId(null);
        assertEquals(expected, actual);
    }

    private void assertInvoiceStatus(InvoiceStatusInfo expected) {
        InvoiceStatusInfo actual = invoiceStatusInfoDao.get(expected.getInvoiceId());
        assertNotNull(actual.getId());
        actual.setId(null);
        assertTrue(actual.getCurrent());
        actual.setCurrent(expected.getCurrent());
        assertEquals(expected, actual);
    }

    private void assertInvoiceCart(List<InvoiceCart> expected) {
        List<InvoiceCart> actual = invoiceCartDao.getByInvoiceId(expected.get(0).getInvoiceId());
        actual.forEach(cart -> {
            assertNotNull(cart.getId());
            cart.setId(null);
        });
        assertEquals(expected, actual);
    }

    private void assertDuplication(String invoiceId) {
        assertEquals(1, countInvoiceEntity(jdbcTemplate, "invoice", invoiceId, false));
        assertEquals(1, countInvoiceEntity(jdbcTemplate, "invoice_status_info", invoiceId, false));
        assertEquals(3, countInvoiceEntity(jdbcTemplate, "invoice_cart", invoiceId, false));
    }

    private void assertTotal() {
        assertEquals(4, countEntities(jdbcTemplate, "invoice"));
        assertEquals(4, countEntities(jdbcTemplate, "invoice_status_info"));
        assertEquals(12, countEntities(jdbcTemplate, "invoice_cart"));
    }
}
