package dev.vality.daway.service;

import dev.vality.damsel.payment_processing.RecurrentPaymentToolEventData;
import dev.vality.daway.handler.event.stock.impl.recurrent.payment.tool.RecurrentPaymentToolHandler;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.sink.common.parser.impl.MachineEventParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class RecurrentPaymentToolService {

    private final List<RecurrentPaymentToolHandler> handlers;
    private final MachineEventParser<RecurrentPaymentToolEventData> parser;

    @Transactional(propagation = Propagation.REQUIRED)
    public void handleEvents(List<MachineEvent> events) {
        events.forEach(event -> {
            AtomicInteger cnt = new AtomicInteger(0);
            var changes = parser.parse(event).getChanges();
            changes.forEach(change -> {
                for (var handler : handlers) {
                    if (handler.accept(change)) {
                        handler.handle(change, event, cnt.getAndIncrement());
                        break;
                    }
                }
            });
        });
    }
}
