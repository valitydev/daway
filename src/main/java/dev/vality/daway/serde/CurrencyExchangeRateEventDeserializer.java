package dev.vality.daway.serde;

import dev.vality.exrates.events.CurrencyEvent;
import dev.vality.kafka.common.serialization.AbstractThriftDeserializer;

public class CurrencyExchangeRateEventDeserializer extends AbstractThriftDeserializer<CurrencyEvent> {
    @Override
    public CurrencyEvent deserialize(String topic, byte[] data) {
        return deserialize(data, new CurrencyEvent());
    }
}
