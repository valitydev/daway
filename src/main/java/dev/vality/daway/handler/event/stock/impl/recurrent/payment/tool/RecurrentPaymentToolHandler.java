package dev.vality.daway.handler.event.stock.impl.recurrent.payment.tool;

import dev.vality.damsel.payment_processing.RecurrentPaymentToolChange;
import dev.vality.daway.handler.event.stock.Handler;
import dev.vality.machinegun.eventsink.MachineEvent;

public interface RecurrentPaymentToolHandler extends Handler<RecurrentPaymentToolChange, MachineEvent> {

}
