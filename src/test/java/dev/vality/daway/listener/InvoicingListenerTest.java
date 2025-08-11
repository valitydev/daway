package dev.vality.daway.listener;

import dev.vality.damsel.payment_processing.Event;
import dev.vality.damsel.payment_processing.EventPayload;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoiceCreated;
import dev.vality.daway.exception.ParseException;
import dev.vality.daway.mapper.invoice.InvoiceCreatedMapper;
import dev.vality.daway.service.InvoiceWrapperService;
import dev.vality.daway.service.InvoicingService;
import dev.vality.daway.service.PartyShopCacheService;
import dev.vality.daway.service.PaymentWrapperService;
import dev.vality.daway.utils.MockUtils;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.machinegun.eventsink.SinkEvent;
import dev.vality.sink.common.parser.impl.MachineEventParser;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;

@ExtendWith(MockitoExtension.class)
public class InvoicingListenerTest {

    @Mock
    private PartyShopCacheService partyShopCacheService;
    @Mock
    private InvoiceWrapperService invoiceBatchService;
    @Mock
    private PaymentWrapperService paymentWrapperService;
    @Mock
    private MachineEventParser eventParser;
    @Mock
    private Acknowledgment ack;

    private InvoicingKafkaListener listener;

    @BeforeEach
    public void init() {
        listener = new InvoicingKafkaListener(new InvoicingService(
                new ArrayList<>(),
                Collections.singletonList(new InvoiceCreatedMapper()),
                new ArrayList<>(),
                partyShopCacheService,
                invoiceBatchService,
                paymentWrapperService,
                eventParser
        ));
    }

    @Test
    public void listenNonInvoiceChanges() {

        MachineEvent message = new MachineEvent();
        Event event = new Event();
        EventPayload payload = new EventPayload();
        event.setPayload(payload);
        Mockito.when(eventParser.parse(message)).thenReturn(payload);

        SinkEvent sinkEvent = new SinkEvent();
        sinkEvent.setEvent(message);

        listener.handle(Collections.singletonList(new ConsumerRecord<>("topic", 1, 1, "kek", sinkEvent)), ack);

        Mockito.verify(invoiceBatchService, Mockito.times(0)).save(anyList());
        Mockito.verify(ack, Mockito.times(1)).acknowledge();
    }

    @Test
    public void listenEmptyException() {
        MachineEvent message = new MachineEvent();

        SinkEvent sinkEvent = new SinkEvent();
        sinkEvent.setEvent(message);

        Mockito.when(eventParser.parse(message)).thenThrow(new ParseException());

        Assertions.assertThrows(ParseException.class, () -> listener.handle(Collections.singletonList(new ConsumerRecord<>("topic", 1, 1, "kek", sinkEvent)), ack));

        Mockito.verify(ack, Mockito.times(0)).acknowledge();
    }

    @Test
    public void listenChanges() {
        MachineEvent message = new MachineEvent();
        message.setCreatedAt(TypeUtil.temporalToString(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS)));
        EventPayload payload = new EventPayload();
        ArrayList<InvoiceChange> invoiceChanges = new ArrayList<>();
        InvoiceChange invoiceChange = new InvoiceChange();
        invoiceChange.setInvoiceCreated(
                new InvoiceCreated(MockUtils.buildInvoice("inv_id")));
        invoiceChanges.add(invoiceChange);
        payload.setInvoiceChanges(invoiceChanges);
        Event event = new Event();
        event.setPayload(payload);
        Mockito.when(eventParser.parse(message)).thenReturn(payload);

        SinkEvent sinkEvent = new SinkEvent();
        sinkEvent.setEvent(message);

        listener.handle(Collections.singletonList(new ConsumerRecord<>("topic", 1, 1, "kek", sinkEvent)), ack);

        Mockito.verify(invoiceBatchService, Mockito.times(1)).save(anyList());
        Mockito.verify(ack, Mockito.times(1)).acknowledge();
    }
}
