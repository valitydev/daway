package dev.vality.daway.kafka;

import dev.vality.daway.integration.base.AbstractKafkaIntegrationTest;
import dev.vality.daway.integration.util.KafkaIntegrationTestSupport;
import dev.vality.daway.config.KafkaPostgresqlSpringBootITest;
import dev.vality.daway.service.DestinationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyList;

@KafkaPostgresqlSpringBootITest
public class DestinationKafkaListenerTest extends AbstractKafkaIntegrationTest {

    @Value("${kafka.topics.destination.id}")
    public String topic;

    @MockitoBean
    private DestinationService destinationService;

    @Test
    public void listenEmptyChanges() {
        KafkaIntegrationTestSupport.sendMessage(embeddedKafkaBroker, topic);
        Mockito.verify(destinationService, Mockito.timeout(TimeUnit.MINUTES.toMillis(1)).times(1))
                .handleEvents(anyList());
    }

}
