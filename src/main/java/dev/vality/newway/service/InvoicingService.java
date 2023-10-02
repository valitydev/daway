package dev.vality.newway.service;

import dev.vality.damsel.payment_processing.EventPayload;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.newway.handler.event.stock.impl.invoicing.InvoicingHandler;
import dev.vality.newway.mapper.Mapper;
import dev.vality.newway.model.InvoiceWrapper;
import dev.vality.newway.model.PaymentWrapper;
import dev.vality.sink.common.parser.impl.MachineEventParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoicingService {

    private final List<InvoicingHandler> otherHandlers;
    private final List<Mapper<InvoiceWrapper>> invoiceMappers;
    private final List<Mapper<PaymentWrapper>> paymentMappers;
    private final PartyShopCacheService partyShopCacheService;
    private final InvoiceWrapperService invoiceWrapperService;
    private final PaymentWrapperService paymentWrapperService;
    private final MachineEventParser<EventPayload> parser;

    @Transactional(propagation = Propagation.REQUIRED)
    public void handleEvents(List<MachineEvent> machineEvents) {
        List<InvoiceWrapper> invoices = new ArrayList<>(machineEvents.size());
        List<PaymentWrapper> payments = new ArrayList<>(machineEvents.size());
        machineEvents.forEach(me -> processMachineEvents(invoices, payments, me));
        if (!invoices.isEmpty()) {
            invoiceWrapperService.save(invoices);
        }
        if (!payments.isEmpty()) {
            paymentWrapperService.save(payments);
        }
    }

    private void processMachineEvents(List<InvoiceWrapper> invoices,
                                      List<PaymentWrapper> payments,
                                      MachineEvent machineEvent) {
        EventPayload payload = parser.parse(machineEvent);
        if (payload.isSetInvoiceChanges()) {
            try {
                List<InvoiceChange> invoiceChanges = payload.getInvoiceChanges();
                for (int changeId = 0; changeId < invoiceChanges.size(); changeId++) {
                    InvoiceChange change = invoiceChanges.get(changeId);
                    handleInvoiceEvent(invoices, change, machineEvent, changeId);
                    handlePaymentEvent(payments, change, machineEvent, changeId);
                    handleOtherEvent(change, machineEvent, changeId);
                }
            } catch (Throwable e) {
                log.error("Unexpected error while handling events; machineId: {},  eventId: {}",
                        machineEvent.getSourceId(), machineEvent.getEventId(), e);
                throw e;
            }
        }
    }

    private void handleOtherEvent(InvoiceChange change, MachineEvent me, int changeId) {
        otherHandlers.stream()
                .filter(m -> m.accept(change))
                .findFirst()
                .ifPresent(m -> m.handle(change, me, changeId));
    }

    private void handlePaymentEvent(List<PaymentWrapper> payments,
                                    InvoiceChange change,
                                    MachineEvent machineEvent,
                                    int changeId) {
        mapEntity(paymentMappers, change, machineEvent, changeId).ifPresent(wrapper -> {
            payments.add(wrapper);
        });
    }

    private void handleInvoiceEvent(List<InvoiceWrapper> invoices,
                                    InvoiceChange change,
                                    MachineEvent machineEvent,
                                    int changeId) {
        mapEntity(invoiceMappers, change, machineEvent, changeId)
                .ifPresent(wrapper -> {
                    invoices.add(wrapper);
                    partyShopCacheService.put(wrapper);
                });
    }

    private <T> Optional<T> mapEntity(List<Mapper<T>> mappers,
                                      InvoiceChange change,
                                      MachineEvent machineEvent,
                                      int changeId) {
        return mappers.stream()
                .filter(m -> m.accept(change))
                .findFirst()
                .map(m -> m.map(change, machineEvent, changeId));
    }
}
