package dev.vality.daway.listener;

import dev.vality.daway.service.ExchangeRateService;
import dev.vality.exrates.events.CurrencyEvent;
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
public class ExchangeRateListener {

    private final ExchangeRateService exchangeRateService;

    @KafkaListener(
            autoStartup = "${kafka.topics.exrate.enabled}",
            topics = "${kafka.topics.exrate.id}",
            containerFactory = "exchangeRateContainerFactory")
    public void handle(List<ConsumerRecord<String, CurrencyEvent>> messages, Acknowledgment ack) {
        log.info("Got ExchangeRate messages batch with size: {}", messages.size());
        exchangeRateService.handleEvents(
                messages.stream()
                        .map(ConsumerRecord::value)
                        .collect(Collectors.toList())
        );
        ack.acknowledge();
        log.info("Batch ExchangeRate has been committed, size={}", messages.size());
    }
}
