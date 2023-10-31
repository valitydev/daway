package dev.vality.daway.listener;

import dev.vality.daway.service.SourceService;
import dev.vality.kafka.common.util.LogUtil;
import dev.vality.machinegun.eventsink.SinkEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class SourceKafkaListener {

    private final SourceService sourceService;

    @KafkaListener(
            autoStartup = "${kafka.topics.source.enabled}",
            topics = "${kafka.topics.source.id}",
            containerFactory = "sourceContainerFactory")
    public void handle(List<ConsumerRecord<String, SinkEvent>> messages, Acknowledgment ack) {
        log.info("Got machineEvent batch with size: {}", messages.size());
        sourceService.handleEvents(messages.stream()
                .map(m -> m.value().getEvent())
                .collect(Collectors.toList()));
        ack.acknowledge();
        log.info("Batch has been committed, size={}, {}", messages.size(),
                LogUtil.toSummaryStringWithSinkEventValues(messages));
    }
}
