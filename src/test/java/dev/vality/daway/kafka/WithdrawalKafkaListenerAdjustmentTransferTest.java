package dev.vality.daway.kafka;

import dev.vality.daway.TestData;
import dev.vality.daway.config.KafkaPostgresqlSpringBootITest;
import dev.vality.daway.dao.withdrawal.iface.FistfulCashFlowDao;
import dev.vality.daway.dao.withdrawal.iface.WithdrawalAdjustmentDao;
import dev.vality.daway.domain.tables.pojos.WithdrawalAdjustment;
import dev.vality.fistful.withdrawal.TimestampedChange;
import dev.vality.kafka.common.serialization.ThriftSerializer;
import dev.vality.machinegun.eventsink.MachineEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;

@KafkaPostgresqlSpringBootITest
class WithdrawalKafkaListenerAdjustmentTransferTest {

    @Value("${kafka.topics.withdrawal.id}")
    public String topic;

    @Autowired
    private KafkaProducer kafkaProducer;

    @MockitoBean
    private WithdrawalAdjustmentDao withdrawalAdjustmentDao;

    @MockitoBean
    private FistfulCashFlowDao fistfulCashFlowDao;

    @BeforeEach
    void setUp() {
        Mockito.reset(withdrawalAdjustmentDao);
        Mockito.reset(fistfulCashFlowDao);
    }

    @Test
    void listenWithdrawalAdjustmentTransferCreatedChange() {
        String adjustmentId = "adjustmentId";
        TimestampedChange timestampedChange = TestData.createWithdrawalAdjustmentTransferCreatedChange(adjustmentId);
        MachineEvent message = new MachineEvent();
        message.setCreatedAt("2023-07-03T10:15:30Z");
        message.setEventId(1L);
        message.setSourceNs("sourceNs");
        message.setSourceId("sourceId");
        message.setData(dev.vality.machinegun.msgpack.Value.bin(new ThriftSerializer<>().serialize("", timestampedChange)));
        WithdrawalAdjustment withdrawalAdjustment = TestData.createWithdrawalAdjustment(adjustmentId);
        withdrawalAdjustment.setId(1L);
        Mockito.when(withdrawalAdjustmentDao.getByIds(anyString(), anyString())).thenReturn(withdrawalAdjustment);
        Mockito.when(withdrawalAdjustmentDao.save(any(WithdrawalAdjustment.class))).thenReturn(Optional.of(1L));
        Mockito.doNothing().when(withdrawalAdjustmentDao).updateNotCurrent(anyLong());

        kafkaProducer.sendMessage(topic, message);

        Mockito.verify(withdrawalAdjustmentDao, Mockito.timeout(TimeUnit.MINUTES.toMillis(2)).times(1))
                .getByIds(anyString(), anyString());
        Mockito.verify(withdrawalAdjustmentDao, Mockito.timeout(TimeUnit.MINUTES.toMillis(1)).times(1))
                .save(any());
        Mockito.verify(fistfulCashFlowDao, Mockito.timeout(TimeUnit.MINUTES.toMillis(1)).times(1))
                .save(anyList());
    }

    @Test
    void listenWithdrawalAdjustmentTransferStatusChange() {
        String adjustmentId = "adjustmentId";
        TimestampedChange timestampedChange = TestData.createWithdrawalAdjustmentTransferStatusChange(adjustmentId);
        MachineEvent message = new MachineEvent();
        message.setCreatedAt("2023-07-03T10:15:30Z");
        message.setEventId(1L);
        message.setSourceNs("sourceNs");
        message.setSourceId("sourceId");
        message.setData(dev.vality.machinegun.msgpack.Value.bin(new ThriftSerializer<>().serialize("", timestampedChange)));
        WithdrawalAdjustment withdrawalAdjustment = TestData.createWithdrawalAdjustment(adjustmentId);
        withdrawalAdjustment.setId(1L);
        Mockito.when(withdrawalAdjustmentDao.getByIds(anyString(), anyString())).thenReturn(withdrawalAdjustment);
        Mockito.when(withdrawalAdjustmentDao.save(any(WithdrawalAdjustment.class))).thenReturn(Optional.of(1L));
        Mockito.doNothing().when(withdrawalAdjustmentDao).updateNotCurrent(anyLong());
        Mockito.when(fistfulCashFlowDao.getByObjId(anyLong(), any())).thenReturn(List.of(TestData.createFistfulCashFlow()));

        kafkaProducer.sendMessage(topic, message);

        Mockito.verify(withdrawalAdjustmentDao, Mockito.timeout(TimeUnit.MINUTES.toMillis(2)).times(1))
                .getByIds(anyString(), anyString());
        Mockito.verify(withdrawalAdjustmentDao, Mockito.timeout(TimeUnit.MINUTES.toMillis(1)).times(1))
                .save(any());
        Mockito.verify(fistfulCashFlowDao, Mockito.timeout(TimeUnit.MINUTES.toMillis(1)).times(1))
                .getByObjId(anyLong(), any());
        Mockito.verify(fistfulCashFlowDao, Mockito.timeout(TimeUnit.MINUTES.toMillis(1)).times(1))
                .save(anyList());
    }
}
