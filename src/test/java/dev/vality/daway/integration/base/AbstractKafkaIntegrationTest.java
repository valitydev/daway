package dev.vality.daway.integration.base;

import dev.vality.daway.integration.util.KafkaIntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

public abstract class AbstractKafkaIntegrationTest extends AbstractPostgresqlIntegrationTest {

    @Autowired
    protected EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @BeforeEach
    void waitForKafkaListenersAssignment() {
        KafkaIntegrationTestSupport.waitForAssignments(kafkaListenerEndpointRegistry, embeddedKafkaBroker);
    }
}
