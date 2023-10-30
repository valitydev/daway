package dev.vality.daway.serde;

import dev.vality.limiter.config.TimestampedChange;
import dev.vality.sink.common.parser.impl.MachineEventParser;
import dev.vality.sink.common.serialization.BinaryDeserializer;
import org.springframework.stereotype.Service;

@Service
public class LimitConfigChangeMachineEventParser extends MachineEventParser<TimestampedChange> {

    public LimitConfigChangeMachineEventParser(BinaryDeserializer<TimestampedChange> deserializer) {
        super(deserializer);
    }
}
