package dev.vality.daway.kafka;

import dev.vality.daway.config.KafkaPostgresqlSpringBootITest;
import dev.vality.daway.service.LimitConfigService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyList;

@KafkaPostgresqlSpringBootITest
public class LimitConfigKafkaListenerTest {

    @Value("${kafka.topics.limit-config.id}")
    public String topic;

    @Autowired
    private KafkaProducer kafkaProducer;

    @MockBean
    private LimitConfigService limitConfigService;

    @Test
    public void listenEmptyChanges() {
        kafkaProducer.sendMessage(topic);
        Mockito.verify(limitConfigService, Mockito.timeout(TimeUnit.MINUTES.toMillis(1)).times(1))
                .handleEvents(anyList());
    }
}
