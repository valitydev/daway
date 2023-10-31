package dev.vality.daway.handler.event.stock.impl.invoicing;

import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.daway.handler.event.stock.Handler;
import dev.vality.machinegun.eventsink.MachineEvent;

public interface InvoicingHandler extends Handler<InvoiceChange, MachineEvent> {
}
