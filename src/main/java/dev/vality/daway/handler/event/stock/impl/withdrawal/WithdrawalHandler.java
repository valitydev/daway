package dev.vality.daway.handler.event.stock.impl.withdrawal;


import dev.vality.daway.handler.event.stock.Handler;
import dev.vality.fistful.withdrawal.TimestampedChange;
import dev.vality.machinegun.eventsink.MachineEvent;

public interface WithdrawalHandler extends Handler<TimestampedChange, MachineEvent> {
}
