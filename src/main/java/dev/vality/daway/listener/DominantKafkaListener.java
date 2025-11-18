package dev.vality.daway.listener;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import dev.vality.damsel.domain_config_v2.HistoricalCommit;
import dev.vality.daway.service.DominantService;
import dev.vality.daway.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
        try {
            dominantService.processCommit(messages.stream()
                    .map(ConsumerRecord::value)
                    .toList());
            ack.acknowledge();
            log.info("Batch has been committed, size={}", messages.size());
        } catch (Exception ex) {
            log.error("Failed to process dominant batch: {}", stringifyMessages(messages), ex);
            throw ex;
        }
    }

    private String stringifyMessages(List<ConsumerRecord<String, HistoricalCommit>> messages) {
        return messages.stream()
                .map(record -> String.format("topic=%s partition=%d offset=%d key=%s payload=%s",
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        record.key(),
                        stringifyCommit(record.value())))
                .collect(Collectors.joining("; "));
    }

    private String stringifyCommit(HistoricalCommit historicalCommit) {
        return historicalCommit == null ? "null" : JsonUtil.thriftBaseToJsonString(historicalCommit);
    }
}
