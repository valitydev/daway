package dev.vality.daway.utils;

import dev.vality.geck.serializer.Geck;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.machinegun.eventsink.SinkEvent;
import dev.vality.machinegun.msgpack.Value;
import dev.vality.xrates.base.TimestampInterval;
import dev.vality.xrates.rate.Change;
import dev.vality.xrates.rate.ExchangeRateCreated;
import dev.vality.xrates.rate.ExchangeRateData;
import dev.vality.xrates.rate.Quote;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class RateSinkEventTestUtils {

    public static List<SinkEvent> create(String sourceId, String... excludedFields) {
        List<Quote> quotes = dev.vality.testcontainers.annotations.util.RandomBeans.randomListOf(4, Quote.class, excludedFields);
        quotes.forEach(quote -> {
            quote.getDestination().setExponent((short) 2);
            quote.getSource().setExponent((short) 2);
            quote.getExchangeRate().setQ(1L);
            quote.getExchangeRate().setP(1L);
        });
        SinkEvent sinkEvent = new SinkEvent();
        sinkEvent.setEvent(new MachineEvent()
                .setEventId(123L)
                .setCreatedAt("2016-03-22T06:12:27Z")
                .setSourceId(sourceId)
                .setData(Value.bin(Geck.toMsgPack(
                        Change.created(
                                new ExchangeRateCreated(
                                        new ExchangeRateData(
                                                new TimestampInterval(
                                                        Instant.now().toString(),
                                                        Instant.now().toString()
                                                ),
                                                quotes
                                        )
                                )
                        ))
                )));
        return Collections.singletonList(sinkEvent);
    }

}
