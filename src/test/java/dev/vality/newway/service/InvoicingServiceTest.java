package dev.vality.newway.service;

import dev.vality.damsel.domain.*;
import dev.vality.damsel.payment_processing.*;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.machinegun.msgpack.Value;
import dev.vality.newway.TestData;
import dev.vality.newway.dao.invoicing.iface.CashFlowDao;
import dev.vality.newway.dao.invoicing.iface.ChargebackDao;
import dev.vality.newway.dao.invoicing.iface.PaymentDao;
import dev.vality.newway.dao.invoicing.iface.PaymentRouteDao;
import dev.vality.newway.domain.enums.PaymentChangeType;
import dev.vality.newway.domain.tables.pojos.Chargeback;
import dev.vality.newway.domain.tables.pojos.Payment;
import dev.vality.newway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.newway.handler.event.stock.impl.invoicing.InvoicingHandler;
import dev.vality.newway.handler.event.stock.impl.invoicing.chargeback.*;
import dev.vality.newway.mapper.Mapper;
import dev.vality.newway.mapper.payment.InvoicePaymentRouteChangedMapper;
import dev.vality.newway.mapper.payment.session.InvoicePaymentSessionChangeFinishedMapper;
import dev.vality.newway.model.InvoiceWrapper;
import dev.vality.sink.common.parser.impl.MachineEventParser;
import dev.vality.sink.common.serialization.impl.PaymentEventPayloadDeserializer;
import dev.vality.sink.common.serialization.impl.PaymentEventPayloadSerializer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class InvoicingServiceTest {

    private final List<Mapper<InvoiceWrapper>> wrongHandlers = new ArrayList<>();
    private final List<Mapper<InvoiceWrapper>> rightHandlers = new ArrayList<>();

    @MockBean
    private InvoiceWrapperService invoiceWrapperService;
    @MockBean
    private PaymentWrapperService paymentWrapperService;
    @MockBean
    private PartyShopCacheService partyShopCacheService;
    @MockBean
    private PaymentRouteDao paymentRouteDao;
    @Mock
    private MachineEventCopyFactory<Chargeback, Integer> machineEventCopyFactory;
    @Mock
    private PaymentDao paymentDao;
    @Mock
    private CashFlowService cashFlowService;
    @Mock
    private MachineEventParser parser;

    @BeforeEach
    public void init() {
        Mapper<InvoiceWrapper> wrong = mock(Mapper.class);
        when(wrong.accept(any())).thenReturn(false);
        wrongHandlers.add(wrong);

        Mapper<InvoiceWrapper> right = mock(Mapper.class);
        when(right.accept(any())).thenReturn(true);
        rightHandlers.add(right);

        when(paymentDao.get(any(), any())).thenReturn(
                dev.vality.testcontainers.annotations.util.RandomBeans.random(Payment.class));

        when(machineEventCopyFactory.create(any(), any(), any(), any())).thenReturn(new Chargeback());
        when(machineEventCopyFactory.create(any(), any(), any(), any(), any())).thenReturn(new Chargeback());
    }

    @Test
    public void handleEmptyChanges() {
        InvoicingService invoicingService = new InvoicingService(
                new ArrayList<>(),
                rightHandlers,
                new ArrayList<>(),
                partyShopCacheService,
                invoiceWrapperService,
                paymentWrapperService,
                parser
        );

        EventPayload eventPayload = new EventPayload();
        when(parser.parse(any())).thenReturn(eventPayload);

        invoicingService.handleEvents(Collections.singletonList(new MachineEvent()));

        verify(rightHandlers.get(0), times(0)).accept(any());
    }

    @Test
    public void handlerSupportsInvoicing() {
        InvoicingService invoicingService = new InvoicingService(
                new ArrayList<>(),
                rightHandlers,
                new ArrayList<>(),
                partyShopCacheService,
                invoiceWrapperService,
                paymentWrapperService,
                parser
        );

        MachineEvent message = new MachineEvent();

        EventPayload eventPayload = new EventPayload();
        eventPayload.setInvoiceChanges(Collections.singletonList(new InvoiceChange()));
        when(parser.parse(any())).thenReturn(eventPayload);

        invoicingService.handleEvents(Collections.singletonList(message));

        verify(rightHandlers.get(0), times(1)).accept(any());
        verify(rightHandlers.get(0), times(1)).map(any(), any(), any());
    }

    @Test
    public void handlerRewrites() {
        InvoicingService invoicingService = new InvoicingService(
                new ArrayList<>(),
                rightHandlers,
                List.of(new InvoicePaymentRouteChangedMapper(),
                        new InvoicePaymentSessionChangeFinishedMapper(paymentRouteDao)),
                partyShopCacheService,
                invoiceWrapperService,
                paymentWrapperService,
                new MachineEventParser<>(new PaymentEventPayloadDeserializer())
        );

        MachineEvent message = TestData.createInvoice(TestData.createPaymentChange());
        MachineEvent message_2 = TestData.createInvoice(TestData.createPaymentChangeRoute());

        EventPayload eventPayload = new EventPayload();
        eventPayload.setInvoiceChanges(Collections.singletonList(new InvoiceChange()));
        when(parser.parse(any())).thenReturn(eventPayload);

        invoicingService.handleEvents(List.of(message_2, message));

        verify(rightHandlers.get(0), times(2)).accept(any());
        verify(rightHandlers.get(0), times(2)).map(any(), any(), any());
    }

    @Test
    public void handlerNotSupportInvoicing() {
        InvoicingService invoicingService = new InvoicingService(
                new ArrayList<>(),
                wrongHandlers,
                new ArrayList<>(),
                partyShopCacheService,
                invoiceWrapperService,
                paymentWrapperService,
                parser
        );

        EventPayload eventPayload = new EventPayload();
        eventPayload.setInvoiceChanges(Collections.singletonList(new InvoiceChange()));
        when(parser.parse(any())).thenReturn(eventPayload);

        invoicingService.handleEvents(Collections.singletonList(new MachineEvent()));

        verify(wrongHandlers.get(0), times(1)).accept(any());
        verify(wrongHandlers.get(0), times(0)).map(any(), any(), any());
    }

    @Test
    public void handlerInvoicePaymentChargebackCreated() {
        ChargebackDao chargebackDao = mock(ChargebackDao.class);
        CashFlowDao cashFlowDao = mock(CashFlowDao.class);

        EventPayload eventPayload = new EventPayload();
        InvoiceChange invoiceChange = TestData.buildInvoiceChangeChargebackCreated();
        eventPayload.setInvoiceChanges(Collections.singletonList(invoiceChange));
        when(parser.parse(any())).thenReturn(eventPayload);

        List<InvoicingHandler> handlers = chargebackHandlers(chargebackDao, cashFlowDao, paymentDao);
        InvoicingService invoicingService = new InvoicingService(
                handlers,
                wrongHandlers,
                new ArrayList<>(),
                partyShopCacheService,
                invoiceWrapperService,
                paymentWrapperService,
                parser);
        MachineEvent machineEvent = buildMachineEvent();
        invoicingService.handleEvents(Collections.singletonList(machineEvent));

        verify(chargebackDao, only()).save(any(Chargeback.class));
    }

    @Test
    public void handlerInvoicePaymentChargebackStatusChanged() {
        ChargebackDao chargebackDao = mockChargebackDao();
        CashFlowDao cashFlowDao = mock(CashFlowDao.class);

        EventPayload eventPayload = new EventPayload();
        InvoiceChange invoiceChange = TestData.buildInvoiceChangeChargebackStatusChanged();
        eventPayload.setInvoiceChanges(Collections.singletonList(invoiceChange));
        when(parser.parse(any())).thenReturn(eventPayload);

        List<InvoicingHandler> handlers = chargebackHandlers(chargebackDao, cashFlowDao, paymentDao);
        InvoicingService invoicingService = new InvoicingService(
                handlers,
                wrongHandlers,
                new ArrayList<>(),
                partyShopCacheService,
                invoiceWrapperService,
                paymentWrapperService,
                parser);

        MachineEvent machineEvent = buildMachineEvent();
        invoicingService.handleEvents(Collections.singletonList(machineEvent));

        verify(chargebackDao, times(1)).save(any(Chargeback.class));
        verify(chargebackDao, times(1)).updateNotCurrent(anyLong());
        verify(cashFlowService, times(1)).save(anyLong(), anyLong(), any(PaymentChangeType.class));
    }

    @Test
    public void handlerInvoicePaymentChargebackLevyChanged() {
        ChargebackDao chargebackDao = mockChargebackDao();
        CashFlowDao cashFlowDao = mock(CashFlowDao.class);

        EventPayload eventPayload = new EventPayload();
        InvoiceChange invoiceChange = TestData.buildInvoiceChangeChargebackLevyChanged();
        eventPayload.setInvoiceChanges(Collections.singletonList(invoiceChange));
        when(parser.parse(any())).thenReturn(eventPayload);

        List<InvoicingHandler> handlers = chargebackHandlers(chargebackDao, cashFlowDao, paymentDao);
        InvoicingService invoicingService = new InvoicingService(
                handlers,
                wrongHandlers,
                new ArrayList<>(),
                partyShopCacheService,
                invoiceWrapperService,
                paymentWrapperService,
                parser);

        MachineEvent machineEvent = buildMachineEvent();
        invoicingService.handleEvents(Collections.singletonList(machineEvent));

        verify(chargebackDao, times(1)).save(any(Chargeback.class));
        verify(chargebackDao, times(1)).updateNotCurrent(anyLong());
        verify(cashFlowService, times(1)).save(anyLong(), anyLong(), any(PaymentChangeType.class));
    }

    @Test
    public void handlerInvoicePaymentChargebackStageChanged() {
        ChargebackDao chargebackDao = mockChargebackDao();
        CashFlowDao cashFlowDao = mock(CashFlowDao.class);

        EventPayload eventPayload = new EventPayload();
        InvoiceChange invoiceChange = TestData.buildInvoiceChangeChargebackStageChanged();
        eventPayload.setInvoiceChanges(Collections.singletonList(invoiceChange));
        when(parser.parse(any())).thenReturn(eventPayload);
        List<InvoicingHandler> handlers = chargebackHandlers(chargebackDao, cashFlowDao, paymentDao);
        InvoicingService invoicingService = new InvoicingService(
                handlers,
                wrongHandlers,
                new ArrayList<>(),
                partyShopCacheService,
                invoiceWrapperService,
                paymentWrapperService,
                parser);

        MachineEvent machineEvent = buildMachineEvent();
        invoicingService.handleEvents(Collections.singletonList(machineEvent));

        verify(chargebackDao, times(1)).save(any(Chargeback.class));
        verify(chargebackDao, times(1)).updateNotCurrent(anyLong());
        verify(cashFlowService, times(1)).save(anyLong(), anyLong(), any(PaymentChangeType.class));
    }

    @Test
    public void handlerInvoicePaymentChargebackCashFlowChanged() {
        ChargebackDao chargebackDao = mockChargebackDao();
        CashFlowDao cashFlowDao = mock(CashFlowDao.class);

        EventPayload eventPayload = new EventPayload();
        InvoiceChange invoiceChange = TestData.buildInvoiceChangeChargebackCashFlowChanged();
        eventPayload.setInvoiceChanges(Collections.singletonList(invoiceChange));
        when(parser.parse(any())).thenReturn(eventPayload);
        List<InvoicingHandler> handlers = chargebackHandlers(chargebackDao, cashFlowDao, paymentDao);
        InvoicingService invoicingService = new InvoicingService(
                handlers,
                wrongHandlers,
                new ArrayList<>(),
                partyShopCacheService,
                invoiceWrapperService,
                paymentWrapperService,
                parser);

        MachineEvent machineEvent = buildMachineEvent();
        invoicingService.handleEvents(Collections.singletonList(machineEvent));

        verify(chargebackDao, times(1)).save(any(Chargeback.class));
        verify(chargebackDao, times(1)).updateNotCurrent(anyLong());
        verify(cashFlowService, times(1)).save(anyLong(), anyLong(), any(PaymentChangeType.class));
        verify(cashFlowDao, times(1)).save(anyList());
    }

    @Test
    public void handlerInvoicePaymentChargebackBodyChanged() {
        ChargebackDao chargebackDao = mockChargebackDao();
        CashFlowDao cashFlowDao = mock(CashFlowDao.class);

        EventPayload eventPayload = new EventPayload();
        InvoiceChange invoiceChange = TestData.buildInvoiceChangeChargebackBodyChanged();
        eventPayload.setInvoiceChanges(Collections.singletonList(invoiceChange));
        when(parser.parse(any())).thenReturn(eventPayload);
        List<InvoicingHandler> handlers = chargebackHandlers(chargebackDao, cashFlowDao, paymentDao);
        InvoicingService invoicingService = new InvoicingService(
                handlers,
                wrongHandlers,
                new ArrayList<>(),
                partyShopCacheService,
                invoiceWrapperService,
                paymentWrapperService,
                parser);

        MachineEvent machineEvent = buildMachineEvent();
        invoicingService.handleEvents(Collections.singletonList(machineEvent));

        verify(chargebackDao, times(1)).save(any(Chargeback.class));
        verify(chargebackDao, times(1)).updateNotCurrent(anyLong());
        verify(cashFlowService, times(1)).save(anyLong(), anyLong(), any(PaymentChangeType.class));
    }

    private ChargebackDao mockChargebackDao() {
        ChargebackDao chargebackDao = mock(ChargebackDao.class);
        when(chargebackDao.get(anyString(), anyString(), anyString()))
                .thenReturn(dev.vality.testcontainers.annotations.util.RandomBeans.random(Chargeback.class));
        when(chargebackDao.save(any(Chargeback.class))).thenReturn(Optional.of(1L));
        return chargebackDao;
    }

    private List<InvoicingHandler> chargebackHandlers(ChargebackDao chargebackDao, CashFlowDao cashFlowDao,
                                                      PaymentDao paymentDao) {
        return Arrays.asList(
                new InvoicePaymentChargebackStageChangedHandler(chargebackDao, cashFlowService,
                        machineEventCopyFactory),
                new InvoicePaymentChargebackBodyChangedHandler(chargebackDao, cashFlowService, machineEventCopyFactory),
                new InvoicePaymentChargebackCashFlowChangedHandler(chargebackDao, cashFlowService, cashFlowDao,
                        machineEventCopyFactory),
                new InvoicePaymentChargebackCreatedHandler(chargebackDao, paymentDao, machineEventCopyFactory),
                new InvoicePaymentChargebackLevyChangedHandler(chargebackDao, cashFlowService, machineEventCopyFactory),
                new InvoicePaymentChargebackStatusChangedHandler(chargebackDao, cashFlowService,
                        machineEventCopyFactory)
        );
    }

    private MachineEvent buildMachineEvent() {
        MachineEvent machineEvent = new MachineEvent();
        machineEvent.setSourceId("testSourceId");
        machineEvent.setCreatedAt(TypeUtil.temporalToString(Instant.now()));

        return machineEvent;
    }

}
