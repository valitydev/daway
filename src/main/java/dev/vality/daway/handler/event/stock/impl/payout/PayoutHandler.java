package dev.vality.daway.handler.event.stock.impl.payout;

import dev.vality.daway.handler.event.stock.Handler;
import dev.vality.payout.manager.Event;
import dev.vality.payout.manager.PayoutChange;

public interface PayoutHandler extends Handler<PayoutChange, Event> {
}
