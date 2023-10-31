package dev.vality.daway.listener;

import dev.vality.daway.service.WithdrawalAdjustmentService;
import dev.vality.kafka.common.util.LogUtil;
import dev.vality.machinegun.eventsink.SinkEvent;
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
public class WithdrawalAdjustmentKafkaListener {

    private final WithdrawalAdjustmentService withdrawalAdjustmentService;

    @KafkaListener(
            autoStartup = "${kafka.topics.withdrawal-adjustment.enabled}",
            topics = "${kafka.topics.withdrawal-adjustment.id}",
            containerFactory = "withdrawalAdjustmentContainerFactory")
    public void handle(List<ConsumerRecord<String, SinkEvent>> messages, Acknowledgment ack) {
        log.info("withdrawalAdjustmentKafkaListener got machineEvent batch with size: {}", messages.size());
        withdrawalAdjustmentService.handleEvents(messages.stream()
                .map(m -> m.value().getEvent())
                .toList());
        ack.acknowledge();
        log.info("withdrawalAdjustmentKafkaListener batch has been committed, size={}, {}", messages.size(),
                LogUtil.toSummaryStringWithSinkEventValues(messages));
    }
}
