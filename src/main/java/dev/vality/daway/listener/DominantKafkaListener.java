package dev.vality.daway.listener;

import dev.vality.damsel.domain_config_v2.HistoricalCommit;
import dev.vality.daway.service.DominantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class DominantKafkaListener {

    private final DominantService dominantService;

    @KafkaListener(
            autoStartup = "${kafka.topics.dominant.enabled}",
            topics = "${kafka.topics.dominant.id}",
            containerFactory = "dominantContainerFactory")
    public void handle(List<ConsumerRecord<String, HistoricalCommit>> messages, Acknowledgment ack) {
        log.info("Got historicalCommit batch with size: {}", messages.size());
        log.debug("HistoricalCommit messages: {}", messages);
        dominantService.processCommit(messages.stream()
                .map(ConsumerRecord::value)
                .toList());
        ack.acknowledge();
        log.info("Batch has been committed, size={}", messages.size());
    }
}
