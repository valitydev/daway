package dev.vality.daway.serde.deserializer;

import dev.vality.fistful.withdrawal_session.TimestampedChange;
import dev.vality.sink.common.serialization.impl.AbstractThriftBinaryDeserializer;
import org.springframework.stereotype.Service;

@Service
public class WithdrawalSessionChangeDeserializer extends AbstractThriftBinaryDeserializer<TimestampedChange> {

    @Override
    public TimestampedChange deserialize(byte[] bin) {
        return deserialize(bin, new TimestampedChange());
    }
}
