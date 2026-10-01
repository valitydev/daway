package dev.vality.daway.kafka;

import dev.vality.daway.integration.base.AbstractKafkaIntegrationTest;
import dev.vality.daway.integration.util.KafkaIntegrationTestSupport;
import dev.vality.daway.config.KafkaPostgresqlSpringBootITest;
import dev.vality.daway.service.DominantService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyList;

@KafkaPostgresqlSpringBootITest
class DominantKafkaListenerTest extends AbstractKafkaIntegrationTest {

    @Value("${kafka.topics.dominant.id}")
    public String topic;

    @MockitoBean
    private DominantService dominantService;

    @Test
    void listenEmptyChanges() {
        KafkaIntegrationTestSupport.sendDominantMessage(embeddedKafkaBroker, topic);
        Mockito.verify(dominantService, Mockito.timeout(TimeUnit.MINUTES.toMillis(1)).times(1))
                .processCommit(anyList());
    }

}
