package dev.vality.daway.service;

import dev.vality.daway.handler.event.stock.impl.withdrawal.WithdrawalAdjustmentHandler;
import dev.vality.daway.handler.event.stock.impl.withdrawal.WithdrawalHandler;
import dev.vality.fistful.withdrawal.TimestampedChange;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.sink.common.parser.impl.MachineEventParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class WithdrawalAdjustmentService {

    private final MachineEventParser<TimestampedChange> parser;
    private final List<WithdrawalAdjustmentHandler> handlers;

    @Transactional(propagation = Propagation.REQUIRED)
    public void handleEvents(List<MachineEvent> machineEvents) {
        machineEvents.forEach(this::handleIfAccept);
    }

    private void handleIfAccept(MachineEvent machineEvent) {
        TimestampedChange eventPayload = parser.parse(machineEvent);
        if (eventPayload.isSetChange()) {
            handlers.stream()
                    .map(WithdrawalHandler.class::cast)
                    .filter(handler -> handler.accept(eventPayload))
                    .forEach(handler -> handler.handle(eventPayload, machineEvent));
        }
    }
}
