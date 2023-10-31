package dev.vality.daway.service;

import dev.vality.daway.handler.event.stock.impl.limiter.LimitConfigHandler;
import dev.vality.limiter.config.TimestampedChange;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.sink.common.parser.impl.MachineEventParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LimitConfigService {

    private final List<LimitConfigHandler> handlers;
    private final MachineEventParser<TimestampedChange> parser;

    @Transactional(propagation = Propagation.REQUIRED)
    public void handleEvents(List<MachineEvent> machineEvents) {
        machineEvents.forEach(this::handleIfAccept);
    }

    private void handleIfAccept(MachineEvent machineEvent) {
        TimestampedChange eventPayload = parser.parse(machineEvent);
        if (eventPayload.isSetChange()) {
            handlers.stream()
                    .filter(handler -> handler.accept(eventPayload))
                    .forEach(handler -> handler.handle(eventPayload, machineEvent));
        }
    }
}
