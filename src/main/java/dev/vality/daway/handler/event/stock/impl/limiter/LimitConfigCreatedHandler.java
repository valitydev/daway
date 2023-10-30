package dev.vality.daway.handler.event.stock.impl.limiter;

import dev.vality.daway.dao.limiter.LimitConfigDao;
import dev.vality.daway.domain.enums.*;
import dev.vality.daway.domain.tables.pojos.LimitConfig;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.daway.util.JsonUtil;
import dev.vality.geck.common.util.TBaseUtil;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.geck.filter.Filter;
import dev.vality.geck.filter.PathConditionFilter;
import dev.vality.geck.filter.condition.IsNullCondition;
import dev.vality.geck.filter.rule.PathConditionRule;
import dev.vality.limiter.config.LimitScopeType;
import dev.vality.limiter.config.TimestampedChange;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class LimitConfigCreatedHandler implements LimitConfigHandler {

    private final LimitConfigDao limitConfigDao;
    private final MachineEventCopyFactory<LimitConfig, String> limitConfigMachineEventCopyFactory;

    @Getter
    private final Filter filter = new PathConditionFilter(new PathConditionRule(
            "change.created",
            new IsNullCondition().not()));

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(TimestampedChange timestampedChange, MachineEvent event) {
        var change = timestampedChange.getChange();
        var limitConfigSource = change.getCreated().getLimitConfig();
        var limitConfigId = limitConfigSource.getId();
        var sequenceId = event.getEventId();
        var sourceId = event.getSourceId();
        log.info("Start LimitConfig created handling, sequenceId={}, limitConfigId={}", sequenceId, limitConfigId);
        var limitConfig = limitConfigMachineEventCopyFactory.create(
                event, sequenceId, sourceId, timestampedChange.getOccuredAt());
        limitConfig.setLimitConfigId(limitConfigId);
        limitConfig.setProcessorType(limitConfigSource.getProcessorType());
        limitConfig.setCreatedAt(TypeUtil.stringToLocalDateTime(limitConfigSource.getCreatedAt()));
        limitConfig.setStartedAt(TypeUtil.stringToLocalDateTime(limitConfigSource.getStartedAt()));
        limitConfig.setShardSize(limitConfigSource.getShardSize());
        if (limitConfigSource.isSetTimeRangeType()) {
            limitConfig.setTimeRangeType(TBaseUtil.unionFieldToEnum(
                    limitConfigSource.getTimeRangeType(), LimitConfigTimeRangeType.class));
            switch (limitConfigSource.getTimeRangeType().getSetField()) {
                case CALENDAR -> limitConfig.setTimeRangeTypeCalendar(TBaseUtil.unionFieldToEnum(
                        limitConfigSource.getTimeRangeType().getCalendar(),
                        LimitConfigTimeRangeTypeCalendar.class));
                case INTERVAL -> limitConfig.setTimeRangeTypeIntervalAmount(
                        limitConfigSource.getTimeRangeType().getInterval().getAmount());
            }
        }
        if (limitConfigSource.isSetContextType()) {
            limitConfig.setLimitContextType(TBaseUtil.unionFieldToEnum(
                    limitConfigSource.getContextType(), LimitConfigLimitContextType.class));
        }
        if (limitConfigSource.isSetType()) {
            if (limitConfigSource.getType().isSetTurnover()) {
                var turnover = limitConfigSource.getType().getTurnover();
                if (turnover.isSetMetric()) {
                    var metric = turnover.getMetric();
                    limitConfig.setLimitTypeTurnoverMetric(
                            TBaseUtil.unionFieldToEnum(metric, LimitConfigLimitTypeTurnoverMetric.class));
                    if (metric.isSetAmount()) {
                        limitConfig.setLimitTypeTurnoverMetricAmountCurrency(metric.getAmount().getCurrency());
                    }
                }
            }
        }
        if (limitConfigSource.isSetScope()) {
            limitConfig.setLimitScope(TBaseUtil.unionFieldToEnum(
                    limitConfigSource.getScope(), LimitConfigLimitScope.class));
            switch (limitConfigSource.getScope().getSetField()) {
                case SINGLE -> limitConfig.setLimitScopeTypesJson(
                        getLimitScopeTypesJson(Set.of(limitConfigSource.getScope().getSingle())));
                case MULTI -> limitConfig.setLimitScopeTypesJson(
                        getLimitScopeTypesJson(limitConfigSource.getScope().getMulti()));
            }
        }
        limitConfig.setDescription(limitConfigSource.getDescription());
        if (limitConfigSource.isSetOpBehaviour() && limitConfigSource.getOpBehaviour().isSetInvoicePaymentRefund()) {
            limitConfig.setOperationLimitBehaviour(TBaseUtil.unionFieldToEnum(
                    limitConfigSource.getOpBehaviour().getInvoicePaymentRefund(),
                    LimitConfigOperationLimitBehaviour.class));
        }

        limitConfigDao.save(limitConfig).ifPresentOrElse(
                dbContractId -> log.info("LimitConfig created has been saved, sequenceId={}, limitConfigId={}",
                        sequenceId, limitConfigId),
                () -> log.info("LimitConfig created bound duplicated, sequenceId={}, limitConfigId={}",
                        sequenceId, limitConfigId));
    }

    private String getLimitScopeTypesJson(Set<LimitScopeType> limitScopeTypes) {
        return JsonUtil.objectToJsonString(limitScopeTypes.stream()
                .map(JsonUtil::thriftBaseToJsonNode)
                .collect(Collectors.toList()));
    }
}
