package dev.vality.daway.config;

import dev.vality.damsel.payment_processing.EventPayload;
import dev.vality.damsel.payment_processing.PartyEventData;
import dev.vality.damsel.payment_processing.RecurrentPaymentToolEventData;
import dev.vality.geck.serializer.Geck;
import dev.vality.sink.common.parser.impl.MachineEventParser;
import dev.vality.sink.common.parser.impl.PartyEventDataMachineEventParser;
import dev.vality.sink.common.parser.impl.PaymentEventPayloadMachineEventParser;
import dev.vality.sink.common.serialization.BinaryDeserializer;
import dev.vality.sink.common.serialization.impl.AbstractThriftBinaryDeserializer;
import dev.vality.sink.common.serialization.impl.PartyEventDataDeserializer;
import dev.vality.sink.common.serialization.impl.PaymentEventPayloadDeserializer;
import dev.vality.xrates.rate.Change;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SuppressWarnings("LineLength")
public class SerializationConfig {

    @Bean
    public BinaryDeserializer<EventPayload> paymentEventPayloadDeserializer() {
        return new PaymentEventPayloadDeserializer();
    }

    @Bean
    public MachineEventParser<EventPayload> paymentEventPayloadMachineEventParser(
            BinaryDeserializer<EventPayload> paymentEventPayloadDeserializer) {
        return new PaymentEventPayloadMachineEventParser(paymentEventPayloadDeserializer);
    }

    @Bean
    public BinaryDeserializer<PartyEventData> partyEventDataBinaryDeserializer() {
        return new PartyEventDataDeserializer();
    }

    @Bean
    public MachineEventParser<PartyEventData> partyEventDataMachineEventParser(
            BinaryDeserializer<PartyEventData> partyEventDataBinaryDeserializer) {
        return new PartyEventDataMachineEventParser(partyEventDataBinaryDeserializer);
    }

    @Bean
    public BinaryDeserializer<RecurrentPaymentToolEventData> recurrentPaymentToolEventDataBinaryDeserializer() {
        return new AbstractThriftBinaryDeserializer<>() {
            @Override
            public RecurrentPaymentToolEventData deserialize(byte[] bytes) {
                return deserialize(bytes, new RecurrentPaymentToolEventData());
            }
        };
    }

    @Bean
    public BinaryDeserializer<Change> rateEventDataBinaryDeserializer() {
        return new AbstractThriftBinaryDeserializer<>() {
            @Override
            public Change deserialize(byte[] bytes) {
                return Geck.msgPackToTBase(bytes, Change.class);
            }
        };
    }

    @Bean
    public MachineEventParser<RecurrentPaymentToolEventData> recurrentPaymentToolEventDataMachineEventParser(
            BinaryDeserializer<RecurrentPaymentToolEventData> recurrentPaymentToolEventDataBinaryDeserializer
    ) {
        return new MachineEventParser<>(recurrentPaymentToolEventDataBinaryDeserializer);
    }

    @Bean
    public BinaryDeserializer<dev.vality.fistful.deposit.Event> depositEventDataBinaryDeserializer() {
        return new AbstractThriftBinaryDeserializer<>() {
            @Override
            public dev.vality.fistful.deposit.Event deserialize(byte[] bytes) {
                return Geck.msgPackToTBase(bytes, dev.vality.fistful.deposit.Event.class);
            }
        };
    }

    @Bean
    public MachineEventParser<dev.vality.fistful.deposit.Event> depositEventDataMachineEventParser(
            BinaryDeserializer<dev.vality.fistful.deposit.Event> depositEventDataBinaryDeserializer) {
        return new MachineEventParser<>(depositEventDataBinaryDeserializer);
    }

    @Bean
    public BinaryDeserializer<dev.vality.fistful.destination.Event> destinationEventDataBinaryDeserializer() {
        return new AbstractThriftBinaryDeserializer<>() {
            @Override
            public dev.vality.fistful.destination.Event deserialize(byte[] bytes) {
                return Geck.msgPackToTBase(bytes, dev.vality.fistful.destination.Event.class);
            }
        };
    }

    @Bean
    public MachineEventParser<dev.vality.fistful.destination.Event> destinationEventDataMachineEventParser(
            BinaryDeserializer<dev.vality.fistful.destination.Event> destinationEventDataBinaryDeserializer) {
        return new MachineEventParser<>(destinationEventDataBinaryDeserializer);
    }

    @Bean
    public BinaryDeserializer<dev.vality.fistful.source.Event> sourceEventDataBinaryDeserializer() {
        return new AbstractThriftBinaryDeserializer<>() {
            @Override
            public dev.vality.fistful.source.Event deserialize(byte[] bytes) {
                return Geck.msgPackToTBase(bytes, dev.vality.fistful.source.Event.class);
            }
        };
    }

    @Bean
    public MachineEventParser<dev.vality.fistful.source.Event> sourceEventDataMachineEventParser(
            BinaryDeserializer<dev.vality.fistful.source.Event> sourceEventDataBinaryDeserializer) {
        return new MachineEventParser<>(sourceEventDataBinaryDeserializer);
    }

    @Bean
    public BinaryDeserializer<dev.vality.fistful.withdrawal.Event> withdrawalEventDataBinaryDeserializer() {
        return new AbstractThriftBinaryDeserializer<>() {
            @Override
            public dev.vality.fistful.withdrawal.Event deserialize(byte[] bytes) {
                return Geck.msgPackToTBase(bytes, dev.vality.fistful.withdrawal.Event.class);
            }
        };
    }

    @Bean
    public MachineEventParser<dev.vality.fistful.withdrawal.Event> withdrawalEventDataMachineEventParser(
            BinaryDeserializer<dev.vality.fistful.withdrawal.Event> withdrawalEventDataBinaryDeserializer) {
        return new MachineEventParser<>(withdrawalEventDataBinaryDeserializer);
    }

    @Bean
    public BinaryDeserializer<dev.vality.fistful.withdrawal_session.Event> withdrawalSessionEventDataBinaryDeserializer() {
        return new AbstractThriftBinaryDeserializer<>() {
            @Override
            public dev.vality.fistful.withdrawal_session.Event deserialize(byte[] bytes) {
                return Geck.msgPackToTBase(bytes, dev.vality.fistful.withdrawal_session.Event.class);
            }
        };
    }

    @Bean
    public MachineEventParser<dev.vality.fistful.withdrawal_session.Event> withdrawalSessionEventDataMachineEventParser(
            BinaryDeserializer<dev.vality.fistful.withdrawal_session.Event> withdrawalSessionEventDataBinaryDeserializer) {
        return new MachineEventParser<>(withdrawalSessionEventDataBinaryDeserializer);
    }
}
