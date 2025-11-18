package dev.vality.daway.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "kafka.consumer")
public class KafkaConsumerProperties {

    private String groupId;
    private int invoicingConcurrency;
    private int recurrentPaymentToolConcurrency;
    private int partyManagementConcurrency;
    private int rateConcurrency;
    private int depositConcurrency;
    private int dominantConcurrency;
    private int withdrawalConcurrency;
    private int payoutConcurrency;
    private int sourceConcurrency;
    private int destinationConcurrency;
    private int withdrawalSessionConcurrency;
    private int limitConfigConcurrency;
    private int exrateConcurrency;
    private int withdrawalAdjustmentConcurrency;
    private long dominantErrorBackoffIntervalMs = 5000L;
    private long dominantErrorMaxAttempts = -1L;

}
