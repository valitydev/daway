package dev.vality.daway.kafka;

import dev.vality.daway.integration.base.AbstractKafkaIntegrationTest;
import dev.vality.daway.integration.util.KafkaIntegrationTestSupport;
import dev.vality.daway.config.KafkaPostgresqlSpringBootITest;
import dev.vality.daway.service.SourceService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyList;

@KafkaPostgresqlSpringBootITest
public class SourceKafkaListenerTest extends AbstractKafkaIntegrationTest {

    @Value("${kafka.topics.source.id}")
    public String topic;

    @MockitoBean
    private SourceService sourceService;

    @Test
    public void listenEmptyChanges() {
        KafkaIntegrationTestSupport.sendMessage(embeddedKafkaBroker, topic);
        Mockito.verify(sourceService, Mockito.timeout(TimeUnit.MINUTES.toMillis(1)).times(1))
                .handleEvents(anyList());
    }

}
