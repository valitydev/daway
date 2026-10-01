package dev.vality.daway.integration.util;

import dev.vality.damsel.domain_config_v2.Author;
import dev.vality.damsel.domain_config_v2.HistoricalCommit;
import dev.vality.kafka.common.serialization.ThriftSerializer;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.machinegun.eventsink.SinkEvent;
import lombok.SneakyThrows;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.thrift.TBase;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.ContainerTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public final class KafkaIntegrationTestSupport {

    private KafkaIntegrationTestSupport() {
    }

    public static void waitForAssignments(KafkaListenerEndpointRegistry registry, EmbeddedKafkaBroker broker) {
        registry.getListenerContainers().stream().filter(container -> container.isRunning()).forEach(container ->
                ContainerTestUtils.waitForAssignment(container, broker.getPartitionsPerTopic()));
    }

    @SneakyThrows
    public static void send(EmbeddedKafkaBroker broker, String topic, TBase<?, ?> payload) {
        var producer = new KafkaProducer<String, byte[]>(producerProperties(broker));
        try {
            byte[] bytes = new ThriftSerializer<>().serialize(topic, payload);
            producer.send(new ProducerRecord<>(topic, bytes)).get(10, TimeUnit.SECONDS);
            producer.flush();
        } finally {
            producer.close(Duration.ofSeconds(5));
        }
    }

    public static void sendMessage(EmbeddedKafkaBroker broker, String topic) {
        MachineEvent message = new MachineEvent();
        dev.vality.machinegun.msgpack.Value data = new dev.vality.machinegun.msgpack.Value();
        data.setBin(new byte[0]);
        message.setCreatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS).format(DateTimeFormatter.ISO_DATE_TIME));
        message.setEventId(1L);
        message.setSourceNs("sad");
        message.setSourceId("sda");
        message.setData(data);
        sendMessage(broker, topic, message);
    }

    public static void sendMessage(EmbeddedKafkaBroker broker, String topic, MachineEvent message) {
        SinkEvent sinkEvent = new SinkEvent();
        sinkEvent.setEvent(message);
        send(broker, topic, sinkEvent);
    }

    public static void sendDominantMessage(EmbeddedKafkaBroker broker, String topic) {
        HistoricalCommit commit = new HistoricalCommit();
        commit.setVersion(1L);
        commit.setCreatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS).format(DateTimeFormatter.ISO_DATE_TIME));
        commit.setChangedBy(new Author()
                .setEmail("email")
                .setId("id")
                .setName("name"));
        commit.setOps(Collections.emptyList());
        send(broker, topic, commit);
    }

    private static Properties producerProperties(EmbeddedKafkaBroker broker) {
        var properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker.getBrokersAsString());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.LINGER_MS_CONFIG, "0");
        properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, "10000");
        return properties;
    }
}
