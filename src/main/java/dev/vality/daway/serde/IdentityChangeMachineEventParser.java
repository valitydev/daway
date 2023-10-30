package dev.vality.daway.serde;

import dev.vality.fistful.identity.TimestampedChange;
import dev.vality.sink.common.parser.impl.MachineEventParser;
import dev.vality.sink.common.serialization.BinaryDeserializer;
import org.springframework.stereotype.Service;

@Service
public class IdentityChangeMachineEventParser extends MachineEventParser<TimestampedChange> {

    public IdentityChangeMachineEventParser(BinaryDeserializer<TimestampedChange> deserializer) {
        super(deserializer);
    }
}