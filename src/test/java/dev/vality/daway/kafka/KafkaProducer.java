package dev.vality.daway.kafka;

import dev.vality.damsel.domain_config_v2.Author;
import dev.vality.damsel.domain_config_v2.HistoricalCommit;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.machinegun.eventsink.SinkEvent;
import dev.vality.kafka.common.serialization.ThriftSerializer;
import jakarta.annotation.PreDestroy;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@TestComponent
@Slf4j
public class KafkaProducer {

    private final org.apache.kafka.clients.producer.KafkaProducer<String, TBase<?, ?>> producer;

    public KafkaProducer(EmbeddedKafkaBroker broker) {
        producer = new org.apache.kafka.clients.producer.KafkaProducer<>(
                KafkaTestUtils.producerProps(broker), new StringSerializer(), new ThriftSerializer<>());
    }

    @SneakyThrows
    public void send(String topic, TBase<?, ?> payload) {
        producer.send(new ProducerRecord<>(topic, payload)).get(10, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void close() {
        producer.close(Duration.ofSeconds(5));
    }

    public void sendMessage(String topic) {
        SinkEvent sinkEvent = new SinkEvent();
        sinkEvent.setEvent(createMessage());
        send(topic, sinkEvent);
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
        send(topic, sinkEvent);
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
        send(topic, commit);
    }
}
