package dev.vality.daway.config;

import dev.vality.daway.kafka.KafkaProducer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@TestExecutionListeners(listeners = KafkaListenerTestExecutionListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@EmbeddedKafka(partitions = 1, topics = {
        "${kafka.topics.invoice.id}",
        "${kafka.topics.recurrent-payment-tool.id}",
        "${kafka.topics.party-management.id}",
        "${kafka.topics.rate.id}",
        "${kafka.topics.dominant.id}",
        "${kafka.topics.deposit.id}",
        "${kafka.topics.withdrawal.id}",
        "${kafka.topics.withdrawal-session.id}",
        "${kafka.topics.source.id}",
        "${kafka.topics.destination.id}",
        "${kafka.topics.limit-config.id}",
        "${kafka.topics.exrate.id}"
}, bootstrapServersProperty = "spring.kafka.bootstrap-servers")
@SpringBootTest(properties = {
        "kafka.topics.invoice.enabled=true",
        "kafka.topics.recurrent-payment-tool.enabled=true",
        "kafka.topics.party-management.enabled=true",
        "kafka.topics.rate.enabled=true",
        "kafka.topics.dominant.enabled=true",
        "kafka.topics.deposit.enabled=true",
        "kafka.topics.withdrawal.enabled=true",
        "kafka.topics.withdrawal-session.enabled=true",
        "kafka.topics.source.enabled=true",
        "kafka.topics.destination.enabled=true",
        "kafka.topics.limit-config.enabled=true",
        "kafka.topics.exrate.enabled=true",
        "kafka.consumer.invoicing-concurrency=1",
        "kafka.consumer.recurrent-payment-tool-concurrency=1",
        "kafka.consumer.party-management-concurrency=1",
        "kafka.consumer.rate-concurrency=1",
        "kafka.consumer.dominant-concurrency=1",
        "kafka.consumer.deposit-concurrency=1",
        "kafka.consumer.withdrawal-concurrency=1",
        "kafka.consumer.withdrawal-session-concurrency=1",
        "kafka.consumer.source-concurrency=1",
        "kafka.consumer.destination-concurrency=1",
        "kafka.consumer.limit-config-concurrency=1",
        "kafka.consumer.exrate-concurrency=1",
        "kafka.consumer.withdrawal-adjustment-concurrency=1"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Import({EmbeddedPostgresqlTestConfiguration.class, KafkaProducer.class})
public @interface KafkaPostgresqlSpringBootITest {
}
