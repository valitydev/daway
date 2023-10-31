package dev.vality.daway.serde;

import dev.vality.fistful.deposit.TimestampedChange;
import dev.vality.sink.common.parser.impl.MachineEventParser;
import dev.vality.sink.common.serialization.BinaryDeserializer;
import org.springframework.stereotype.Service;

@Service
public class DepositChangeMachineEventParser extends MachineEventParser<TimestampedChange> {

    public DepositChangeMachineEventParser(BinaryDeserializer<TimestampedChange> deserializer) {
        super(deserializer);
    }

}