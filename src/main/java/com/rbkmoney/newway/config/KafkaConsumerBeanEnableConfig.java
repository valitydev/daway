package com.rbkmoney.newway.config;

import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.newway.poller.listener.InvoicingKafkaListener;
import com.rbkmoney.newway.service.InvoicingService;
import com.rbkmoney.sink.common.parser.impl.MachineEventParser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
public class KafkaConsumerBeanEnableConfig {

    @Bean
    @ConditionalOnProperty(value = "kafka.topics.invoice.enabled", havingValue = "true")
    public InvoicingKafkaListener paymentEventsKafkaListener(InvoicingService invoicingService,
                                                             MachineEventParser<EventPayload> parser) {
        return new InvoicingKafkaListener(invoicingService, parser);
    }
}
