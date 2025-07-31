package dev.vality.daway.kafka;

import dev.vality.damsel.domain_config_v2.Author;
import dev.vality.damsel.domain_config_v2.HistoricalCommit;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.machinegun.eventsink.SinkEvent;
import dev.vality.testcontainers.annotations.kafka.config.KafkaProducerConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

@TestComponent
@Import(KafkaProducerConfig.class)
@Slf4j
public class KafkaProducer {

    @Autowired
    private dev.vality.testcontainers.annotations.kafka.config.KafkaProducer<TBase<?, ?>> testThriftKafkaProducer;

    public void sendMessage(String topic) {
        SinkEvent sinkEvent = new SinkEvent();
        sinkEvent.setEvent(createMessage());
        testThriftKafkaProducer.send(topic, sinkEvent);
    }

    private MachineEvent createMessage() {
        MachineEvent message = new MachineEvent();
        dev.vality.machinegun.msgpack.Value data = new dev.vality.machinegun.msgpack.Value();
        data.setBin(new byte[0]);
        message.setCreatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS).format(DateTimeFormatter.ISO_DATE_TIME));
        message.setEventId(1L);
        message.setSourceNs("sad");
        message.setSourceId("sda");
        message.setData(data);
        return message;
    }

    public void sendMessage(String topic, MachineEvent message) {
        SinkEvent sinkEvent = new SinkEvent();
        sinkEvent.setEvent(message);
        testThriftKafkaProducer.send(topic, sinkEvent);
    }

    public void sendDominantMessage(String topic) {
        HistoricalCommit commit = new HistoricalCommit();
        commit.setVersion(1L);
        commit.setCreatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS).format(DateTimeFormatter.ISO_DATE_TIME));
        commit.setChangedBy(new Author()
                .setEmail("email")
                .setId("id")
                .setName("name"));
        commit.setOps(Collections.emptyList());
        testThriftKafkaProducer.send(topic, commit);
    }
}
