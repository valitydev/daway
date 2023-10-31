package dev.vality.daway.handler.event.stock.impl.source;

import dev.vality.daway.dao.source.iface.SourceDao;
import dev.vality.daway.domain.enums.SourceStatus;
import dev.vality.daway.domain.tables.pojos.Source;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.fistful.source.Change;
import dev.vality.fistful.source.Status;
import dev.vality.fistful.source.TimestampedChange;
import dev.vality.geck.common.util.TBaseUtil;
import dev.vality.geck.filter.Filter;
import dev.vality.geck.filter.PathConditionFilter;
import dev.vality.geck.filter.condition.IsNullCondition;
import dev.vality.geck.filter.rule.PathConditionRule;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SourceStatusChangedHandler implements SourceHandler {

    private final SourceDao sourceDao;
    private final MachineEventCopyFactory<Source, String> sourceMachineEventCopyFactory;

    @Getter
    private final Filter filter = new PathConditionFilter(
            new PathConditionRule("change.status.status", new IsNullCondition().not()));

    @Override
    public void handle(TimestampedChange timestampedChange, MachineEvent event) {
        Change change = timestampedChange.getChange();
        Status status = change.getStatus().getStatus();
        long sequenceId = event.getEventId();
        String sourceId = event.getSourceId();
        log.info("Start source status changed handling, sequenceId={}, sourceId={}", sequenceId, sourceId);

        final Source sourceOld = sourceDao.get(sourceId);
        Source sourceNew = sourceMachineEventCopyFactory
                .create(event, sequenceId, sourceId, sourceOld, timestampedChange.getOccuredAt());

        sourceNew.setSourceStatus(TBaseUtil.unionFieldToEnum(status, SourceStatus.class));

        sourceDao.save(sourceNew).ifPresentOrElse(
                id -> {
                    sourceDao.updateNotCurrent(sourceOld.getId());
                    log.info("Source status have been changed, sequenceId={}, sourceId={}", sequenceId, sourceId);
                },
                () -> log.info("Source status bound duplicated, sequenceId={}, sourceId={}", sequenceId, sourceId));
    }

}
