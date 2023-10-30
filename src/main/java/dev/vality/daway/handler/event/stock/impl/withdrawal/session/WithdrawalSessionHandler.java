package dev.vality.daway.handler.event.stock.impl.withdrawal.session;

import dev.vality.daway.handler.event.stock.Handler;
import dev.vality.fistful.withdrawal_session.TimestampedChange;
import dev.vality.machinegun.eventsink.MachineEvent;

public interface WithdrawalSessionHandler extends Handler<TimestampedChange, MachineEvent> {
}
