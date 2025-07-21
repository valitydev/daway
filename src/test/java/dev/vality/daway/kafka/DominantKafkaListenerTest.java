package dev.vality.daway.kafka;

import dev.vality.daway.config.KafkaPostgresqlSpringBootITest;
import dev.vality.daway.service.DominantService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyList;

@KafkaPostgresqlSpringBootITest
public class DominantKafkaListenerTest {

    @Value("${kafka.topics.dominant.id}")
    public String topic;

    @Autowired
    private KafkaProducer kafkaProducer;

    @MockitoBean
    private DominantService dominantService;

    @Test
    public void listenEmptyChanges() {
        kafkaProducer.sendMessage(topic);
        Mockito.verify(dominantService, Mockito.timeout(TimeUnit.MINUTES.toMillis(1)).times(1))
                .processCommit(anyList());
    }

}
