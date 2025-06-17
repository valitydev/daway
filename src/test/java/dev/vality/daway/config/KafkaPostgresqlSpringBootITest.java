package dev.vality.daway.config;

import dev.vality.daway.kafka.KafkaProducer;
import dev.vality.testcontainers.annotations.DefaultSpringBootTest;
import dev.vality.testcontainers.annotations.kafka.KafkaTestcontainerSingleton;
import dev.vality.testcontainers.annotations.postgresql.PostgresqlTestcontainerSingleton;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PostgresqlTestcontainerSingleton
@KafkaTestcontainerSingleton(
        properties = {
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
                "kafka.topics.exrate.enabled=true"},
        topicsKeys = {
                "kafka.topics.invoice.id",
                "kafka.topics.recurrent-payment-tool.id",
                "kafka.topics.party-management.id",
                "kafka.topics.rate.id",
                "kafka.topics.dominant.id",
                "kafka.topics.deposit.id",
                "kafka.topics.withdrawal.id",
                "kafka.topics.withdrawal-session.id",
                "kafka.topics.source.id",
                "kafka.topics.destination.id",
                "kafka.topics.limit-config.id",
                "kafka.topics.limit-config.id",
                "kafka.topics.exrate.id"}
)
@DefaultSpringBootTest
@Import(KafkaProducer.class)
public @interface KafkaPostgresqlSpringBootITest {
}
