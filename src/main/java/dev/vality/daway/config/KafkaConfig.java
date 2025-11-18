package dev.vality.daway.config;

import java.util.Map;
import java.util.Objects;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import dev.vality.damsel.domain_config_v2.HistoricalCommit;
import dev.vality.daway.config.properties.KafkaConsumerProperties;
import dev.vality.daway.serde.CurrencyExchangeRateEventDeserializer;
import dev.vality.daway.serde.HistoricalCommitDeserializer;
import dev.vality.daway.serde.SinkEventDeserializer;
import dev.vality.daway.service.FileService;
import dev.vality.daway.util.JsonUtil;
import dev.vality.exrates.events.CurrencyEvent;
import dev.vality.kafka.common.util.ExponentialBackOffDefaultErrorHandlerFactory;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("LineLength")
public class KafkaConfig {

    private final KafkaProperties kafkaProperties;
    private final KafkaConsumerProperties kafkaConsumerProperties;
    private final FileService fileService;

    @Value("${kafka.topics.party-management.consumer.group-id}")
    private String partyConsumerGroup;

    @Value("${kafka.topics.exrate.consumer.group-id}")
    private String exrateConsumerGroup;

    @Value("${kafka.topics.withdrawal-adjustment.consumer.group-id}")
    private String withdrawalAdjustmentConsumerGroup;

    @Value("${kafka.rack.path:/tmp/.kafka_rack_env}")
    private String rackPath;

    @Bean
    public Map<String, Object> consumerConfigs() {
        return createConsumerConfig();
    }

    @Bean
    public Map<String, Object> consumerDominantConfigs() {
        return createConsumerDominantConfig();
    }

    private Map<String, Object> createConsumerConfig() {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SinkEventDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConsumerProperties.getGroupId());
        String clientRack = fileService.getClientRack(rackPath);
        if (Objects.nonNull(clientRack)) {
            props.put(ConsumerConfig.CLIENT_RACK_CONFIG, clientRack);
        }
        return props;
    }

    private Map<String, Object> createConsumerDominantConfig() {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, HistoricalCommitDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConsumerProperties.getGroupId());
        String clientRack = fileService.getClientRack(rackPath);
        if (Objects.nonNull(clientRack)) {
            props.put(ConsumerConfig.CLIENT_RACK_CONFIG, clientRack);
        }
        return props;
    }

    @Bean
    public ConsumerFactory<String, MachineEvent> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public ConsumerFactory<String, HistoricalCommit> consumerDominantFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerDominantConfigs());
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, MachineEvent>> kafkaListenerContainerFactory(
            ConsumerFactory<String, MachineEvent> consumerFactory) {
        return createConcurrentFactory(consumerFactory, kafkaConsumerProperties.getInvoicingConcurrency());
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, MachineEvent>> recPayToolContainerFactory(
            ConsumerFactory<String, MachineEvent> consumerFactory) {
        return createConcurrentFactory(consumerFactory, kafkaConsumerProperties.getRecurrentPaymentToolConcurrency());
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, MachineEvent>> rateContainerFactory(
            ConsumerFactory<String, MachineEvent> consumerFactory) {
        return createConcurrentFactory(consumerFactory, kafkaConsumerProperties.getRateConcurrency());
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, MachineEvent>> depositContainerFactory(
            ConsumerFactory<String, MachineEvent> consumerFactory) {
        return createConcurrentFactory(consumerFactory, kafkaConsumerProperties.getDepositConcurrency());
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, HistoricalCommit>> dominantContainerFactory(
            ConsumerFactory<String, HistoricalCommit> consumerFactory,
            DefaultErrorHandler dominantErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, HistoricalCommit> factory =
                createConcurrentFactory(consumerFactory, kafkaConsumerProperties.getDominantConcurrency());
        factory.setCommonErrorHandler(dominantErrorHandler);
        return factory;
    }

    @Bean
    public DefaultErrorHandler dominantErrorHandler() {
        long interval = kafkaConsumerProperties.getDominantErrorBackoffIntervalMs();
        long maxAttempts = kafkaConsumerProperties.getDominantErrorMaxAttempts();
        FixedBackOff backOff = new FixedBackOff(interval, resolveDominantMaxAttempts(maxAttempts));
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(backOff);
        errorHandler.setRetryListeners((record, ex, deliveryAttempt) -> {
            if (record != null) {
                log.error("Failed to process HistoricalCommit, attempt={}, topic={}, partition={}, offset={}, key={}, payload={}",
                        deliveryAttempt,
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        record.key(),
                        JsonUtil.thriftBaseToJsonString((HistoricalCommit) record.value()),
                        ex);
            } else {
                log.error("Failed to process HistoricalCommit, attempt={}", deliveryAttempt, ex);
            }
        });
        return errorHandler;
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, MachineEvent>> withdrawalContainerFactory(
            ConsumerFactory<String, MachineEvent> consumerFactory) {
        return createConcurrentFactory(consumerFactory, kafkaConsumerProperties.getWithdrawalConcurrency());
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, MachineEvent>> withdrawalAdjustmentContainerFactory() {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SinkEventDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, withdrawalAdjustmentConsumerGroup);
        String clientRack = fileService.getClientRack(rackPath);
        if (Objects.nonNull(clientRack)) {
            props.put(ConsumerConfig.CLIENT_RACK_CONFIG, clientRack);
        }
        ConsumerFactory<String, MachineEvent> consumerFactory = new DefaultKafkaConsumerFactory<>(props);
        return createConcurrentFactory(consumerFactory, kafkaConsumerProperties.getWithdrawalAdjustmentConcurrency());
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, MachineEvent>> sourceContainerFactory(
            ConsumerFactory<String, MachineEvent> consumerFactory) {
        return createConcurrentFactory(consumerFactory, kafkaConsumerProperties.getSourceConcurrency());
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, MachineEvent>> destinationContainerFactory(
            ConsumerFactory<String, MachineEvent> consumerFactory) {
        return createConcurrentFactory(consumerFactory, kafkaConsumerProperties.getDestinationConcurrency());
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, MachineEvent>> withdrawalSessionContainerFactory(
            ConsumerFactory<String, MachineEvent> consumerFactory) {
        return createConcurrentFactory(consumerFactory, kafkaConsumerProperties.getWithdrawalSessionConcurrency());
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, MachineEvent>> partyManagementContainerFactory() {
        Map<String, Object> configs = createConsumerConfig();
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, partyConsumerGroup);
        ConsumerFactory<String, MachineEvent> consumerFactory = new DefaultKafkaConsumerFactory<>(configs);
        return createConcurrentFactory(consumerFactory, kafkaConsumerProperties.getPartyManagementConcurrency());
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, MachineEvent>> limitConfigContainerFactory(
            ConsumerFactory<String, MachineEvent> consumerFactory) {
        return createConcurrentFactory(consumerFactory, kafkaConsumerProperties.getLimitConfigConcurrency());
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, CurrencyEvent>> exchangeRateContainerFactory() {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, CurrencyExchangeRateEventDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, exrateConsumerGroup);
        ConsumerFactory<String, CurrencyEvent> consumerFactory = new DefaultKafkaConsumerFactory<>(props);

        return createConcurrentFactory(consumerFactory, kafkaConsumerProperties.getExrateConcurrency());
    }

    private long resolveDominantMaxAttempts(long configuredMaxAttempts) {
        return configuredMaxAttempts < 0 ? FixedBackOff.UNLIMITED_ATTEMPTS : configuredMaxAttempts;
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> createConcurrentFactory(
            ConsumerFactory<String, T> consumerFactory, int threadsNumber) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        initFactory(consumerFactory, threadsNumber, factory);
        return factory;
    }

    private <T> void initFactory(ConsumerFactory<String, T> consumerFactory,
                                 int threadsNumber,
                                 ConcurrentKafkaListenerContainerFactory<String, T> factory) {
        factory.setConsumerFactory(consumerFactory);
        factory.setBatchListener(true);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(ExponentialBackOffDefaultErrorHandlerFactory.create());
        factory.setConcurrency(threadsNumber);
    }
}
