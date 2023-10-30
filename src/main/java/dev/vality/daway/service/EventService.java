package dev.vality.daway.service;

import java.util.Optional;

public interface EventService<EventT, PayloadT> {
    Optional<Long> getLastEventId();

    void handleEvents(EventT processingEvent, PayloadT payload);
}
