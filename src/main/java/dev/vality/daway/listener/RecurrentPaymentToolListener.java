package dev.vality.daway.listener;

import dev.vality.daway.service.RecurrentPaymentToolService;
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
public class RecurrentPaymentToolListener {

    private final RecurrentPaymentToolService recurrentPaymentToolService;

    @KafkaListener(
            autoStartup = "${kafka.topics.recurrent-payment-tool.enabled}",
            topics = "${kafka.topics.recurrent-payment-tool.id}",
            containerFactory = "recPayToolContainerFactory")
    public void handle(List<ConsumerRecord<String, SinkEvent>> messages, Acknowledgment ack) {
        log.info("Got recPayTool machineEvent batch with size: {}", messages.size());
        recurrentPaymentToolService.handleEvents(
                messages.stream()
                        .map(m -> m.value().getEvent())
                        .collect(Collectors.toList())
        );
        ack.acknowledge();
        log.info("Batch recPayTool has been committed, size={}, {}", messages.size(),
                LogUtil.toSummaryStringWithSinkEventValues(messages));
    }
}
