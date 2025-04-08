package dev.vality.daway.kafka;

import dev.vality.daway.TestData;
import dev.vality.daway.config.KafkaPostgresqlSpringBootITest;
import dev.vality.daway.dao.withdrawal.iface.WithdrawalAdjustmentDao;
import dev.vality.daway.dao.withdrawal.iface.WithdrawalDao;
import dev.vality.fistful.withdrawal.TimestampedChange;
import dev.vality.kafka.common.serialization.ThriftSerializer;
import dev.vality.machinegun.eventsink.MachineEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;

@KafkaPostgresqlSpringBootITest
@TestPropertySource(properties = {"kafka.topics.withdrawal-adjustment.enabled=true"})
class WithdrawalAdjustmentKafkaListenerTest {

    @Value("${kafka.topics.withdrawal.id}")
    public String topic;

    @Autowired
    private KafkaProducer kafkaProducer;

    @MockitoBean
    private WithdrawalAdjustmentDao withdrawalAdjustmentDao;

    @MockitoBean
    private WithdrawalDao withdrawalDao;

    @BeforeEach
    void setUp() {
        Mockito.reset(withdrawalAdjustmentDao);
        Mockito.reset(withdrawalDao);
    }

    @Test
    void listenWithdrawalAdjustmentCreatedChange() {
        TimestampedChange timestampedChange = TestData.createWithdrawalAdjustmentCreatedChange("adjustmentId");
        MachineEvent message = new MachineEvent();
        message.setCreatedAt("2023-07-03T10:15:30Z");
        message.setEventId(1L);
        message.setSourceNs("sourceNs");
        message.setSourceId("sourceId");
        message.setData(dev.vality.machinegun.msgpack.Value.bin(new ThriftSerializer<>().serialize("", timestampedChange)));

        kafkaProducer.sendMessage(topic, message);

        Mockito.verify(withdrawalAdjustmentDao, Mockito.timeout(TimeUnit.MINUTES.toMillis(1)).atLeastOnce())
                .save(any());
    }

    @Test
    void doNotListenWithdrawalChange() {
        TimestampedChange timestampedChange = TestData.createWithdrawalCreatedChange("withdrawalId");
        MachineEvent message = new MachineEvent();
        message.setCreatedAt("2023-07-03T10:15:30Z");
        message.setEventId(1L);
        message.setSourceNs("sourceNs");
        message.setSourceId("sourceId");
        message.setData(dev.vality.machinegun.msgpack.Value.bin(new ThriftSerializer<>().serialize("", timestampedChange)));

        kafkaProducer.sendMessage(topic, message);
        Mockito.verify(withdrawalDao, Mockito.after(TimeUnit.MINUTES.toMillis(1)).only()).save(any());
    }
}
