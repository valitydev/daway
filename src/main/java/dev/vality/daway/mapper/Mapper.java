package dev.vality.daway.mapper;

import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.geck.filter.Filter;
import dev.vality.machinegun.eventsink.MachineEvent;

public interface Mapper<M> {
    default boolean accept(InvoiceChange change) {
        return getFilter().match(change);
    }

    M map(InvoiceChange change, MachineEvent event, Integer changeId);

    Filter getFilter();
}
