package dev.vality.daway.handler.event.stock.impl.invoicing.chargeback;

import dev.vality.damsel.domain.FinalCashFlowPosting;
import dev.vality.damsel.payment_processing.EventPayload;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.daway.TestData;
import dev.vality.daway.config.PostgresqlJooqSpringBootITest;
import dev.vality.daway.dao.invoicing.impl.CashFlowDaoImpl;
import dev.vality.daway.dao.invoicing.impl.ChargebackDaoImpl;
import dev.vality.daway.domain.tables.pojos.Chargeback;
import dev.vality.daway.domain.tables.records.ChargebackRecord;
import dev.vality.daway.factory.machine.event.ChargebackMachineEventCopyFactoryImpl;
import dev.vality.daway.service.CashFlowService;
import dev.vality.machinegun.eventsink.MachineEvent;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static dev.vality.daway.domain.tables.CashFlow.CASH_FLOW;
import static dev.vality.daway.domain.tables.Chargeback.CHARGEBACK;
import static org.junit.jupiter.api.Assertions.assertEquals;

@PostgresqlJooqSpringBootITest
@ContextConfiguration(classes = {InvoicePaymentChargebackCashFlowChangedHandler.class, ChargebackDaoImpl.class,
        CashFlowService.class, CashFlowDaoImpl.class,
        ChargebackMachineEventCopyFactoryImpl.class,})
class InvoicePaymentChargebackCashFlowChangedHandlerTest {

    @Autowired
    InvoicePaymentChargebackCashFlowChangedHandler handler;

    @Autowired
    DSLContext dslContext;

    @BeforeEach
    void setUp() {
        dslContext.deleteFrom(CHARGEBACK).execute();
        dslContext.deleteFrom(CASH_FLOW).execute();
    }

    @Test
    void handle() {
        String chargebackId = "testChargebackId";
        Chargeback chargeback = TestData.createChargeback(chargebackId);
        dslContext.insertInto(CHARGEBACK)
                .set(dslContext.newRecord(CHARGEBACK, chargeback))
                .execute();
        List<FinalCashFlowPosting> finalCashFlowPostings = TestData.buildCashFlowPostings();
        InvoiceChange invoiceChange = TestData.buildInvoiceChangeChargebackCashFlowChanged(finalCashFlowPostings);
        EventPayload eventPayload = new EventPayload();
        eventPayload.setInvoiceChanges(List.of(invoiceChange));
        MachineEvent event = TestData.createMachineEvent(eventPayload, chargeback.getInvoiceId());
        event.setSourceId(chargeback.getInvoiceId());

        handler.handle(invoiceChange, event, 4);

        Result<ChargebackRecord> recordNew = dslContext.fetch(CHARGEBACK, CHARGEBACK.CURRENT.eq(Boolean.TRUE));
        assertEquals(1, recordNew.size());
        assertEquals(200, recordNew.get(0).getChargebackFee());
        assertEquals(100, recordNew.get(0).getChargebackProviderFee());
        assertEquals(100, recordNew.get(0).getChargebackExternalFee());
        assertEquals(finalCashFlowPostings.size(), dslContext.fetchCount(CASH_FLOW));


    }
}