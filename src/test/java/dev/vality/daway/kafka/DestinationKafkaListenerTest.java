package dev.vality.daway.kafka;

import dev.vality.daway.config.KafkaPostgresqlSpringBootITest;
import dev.vality.daway.service.DestinationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyList;

@KafkaPostgresqlSpringBootITest
public class DestinationKafkaListenerTest {

    @Value("${kafka.topics.destination.id}")
    public String topic;

    @Autowired
    private KafkaProducer kafkaProducer;

    @MockBean
    private DestinationService destinationService;

    @Test
    public void listenEmptyChanges() {
        kafkaProducer.sendMessage(topic);
        Mockito.verify(destinationService, Mockito.timeout(TimeUnit.MINUTES.toMillis(1)).times(1))
                .handleEvents(anyList());
    }

}
