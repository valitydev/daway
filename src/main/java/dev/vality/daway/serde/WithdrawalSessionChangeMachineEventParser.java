package dev.vality.daway.serde;

import dev.vality.fistful.withdrawal_session.TimestampedChange;
import dev.vality.sink.common.parser.impl.MachineEventParser;
import dev.vality.sink.common.serialization.BinaryDeserializer;
import org.springframework.stereotype.Service;

@Service
public class WithdrawalSessionChangeMachineEventParser extends MachineEventParser<TimestampedChange> {

    public WithdrawalSessionChangeMachineEventParser(BinaryDeserializer<TimestampedChange> deserializer) {
        super(deserializer);
    }
}