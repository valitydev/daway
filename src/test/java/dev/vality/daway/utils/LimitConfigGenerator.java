package dev.vality.daway.utils;

import dev.vality.geck.common.util.TypeUtil;
import dev.vality.limiter.config.*;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.SneakyThrows;
import org.apache.thrift.TBase;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static dev.vality.testcontainers.annotations.util.RandomBeans.randomThriftOnlyRequiredFields;

public class LimitConfigGenerator {

    public static List<MachineEvent> getEvents(
            String sourceId,
            long sequenceId,
            Change change) {
        return List.of(getEvent(sourceId, sequenceId, change));
    }

    public static MachineEvent getEvent(
            String sourceId,
            long sequenceId,
            Change change) {
        return new MachineEvent()
                .setData(toByteArray(new TimestampedChange()
                        .setChange(change)
                        .setOccuredAt(getTemporal())))
                .setCreatedAt(Instant.now().toString())
                .setEventId(sequenceId)
                .setSourceNs("source_ns")
                .setSourceId(sourceId);
    }

    public static Change getCreated(LimitConfig limitConfig) {
        return Change.created(new CreatedChange(limitConfig));
    }

    public static LimitConfig getLimitConfig(String limitConfigId) {
        return randomThriftOnlyRequiredFields(LimitConfig.class)
                .setId(limitConfigId)
                .setType(LimitType.turnover(new LimitTypeTurnover().setMetric(LimitTurnoverMetric.amount(new LimitTurnoverAmount("RUB")))))
                .setScope(LimitScope.multi(Set.of(LimitScopeType.identity(new LimitScopeEmptyDetails()),
                        LimitScopeType.party(new LimitScopeEmptyDetails()),
                        LimitScopeType.shop(new LimitScopeEmptyDetails()))))
                .setDescription("asd")
                .setCreatedAt(getTemporal())
                .setStartedAt(getTemporal())
                .setOpBehaviour(new OperationLimitBehaviour().setInvoicePaymentRefund(OperationBehaviour.addition(new Addition())));
    }

    @SneakyThrows
    private static dev.vality.machinegun.msgpack.Value toByteArray(TBase<?, ?> thrift) {
        return dev.vality.machinegun.msgpack.Value.bin(
                new TSerializer(new TBinaryProtocol.Factory())
                        .serialize(thrift));
    }

    private static String getTemporal() {
        return TypeUtil.temporalToString(Instant.now());
    }
}
