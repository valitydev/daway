package dev.vality.daway.kafka;

import dev.vality.daway.integration.base.AbstractKafkaIntegrationTest;
import dev.vality.daway.integration.util.KafkaIntegrationTestSupport;
import dev.vality.daway.config.KafkaPostgresqlSpringBootITest;
import dev.vality.daway.service.DepositService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyList;

@KafkaPostgresqlSpringBootITest
public class DepositKafkaListenerTest extends AbstractKafkaIntegrationTest {

    @Value("${kafka.topics.deposit.id}")
    public String topic;

    @MockitoBean
    private DepositService depositService;

    @Test
    public void listenEmptyChanges() {
        KafkaIntegrationTestSupport.sendMessage(embeddedKafkaBroker, topic);
        Mockito.verify(depositService, Mockito.timeout(TimeUnit.MINUTES.toMillis(1)).times(1))
                .handleEvents(anyList());
    }
}
