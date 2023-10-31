package dev.vality.daway.handler.event.stock.impl.source;

import dev.vality.daway.dao.source.iface.SourceDao;
import dev.vality.daway.domain.enums.SourceStatus;
import dev.vality.daway.domain.tables.pojos.Source;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.fistful.source.Change;
import dev.vality.fistful.source.Internal;
import dev.vality.fistful.source.Resource;
import dev.vality.fistful.source.TimestampedChange;
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
public class SourceCreatedHandler implements SourceHandler {

    private final SourceDao sourceDao;
    private final MachineEventCopyFactory<Source, String> sourceMachineEventCopyFactory;

    @Getter
    private final Filter filter = new PathConditionFilter(
            new PathConditionRule("change.created", new IsNullCondition().not()));

    @Override
    public void handle(TimestampedChange timestampedChange, MachineEvent event) {
        Change change = timestampedChange.getChange();
        long sequenceId = event.getEventId();
        String sourceId = event.getSourceId();
        log.info("Start source created handling, sequenceId={}, sourceId={}", sequenceId, sourceId);

        Source source =
                sourceMachineEventCopyFactory.create(event, sequenceId, sourceId, timestampedChange.getOccuredAt());

        source.setSourceName(change.getCreated().getName());
        source.setSourceStatus(SourceStatus.unauthorized);
        source.setExternalId(change.getCreated().getExternalId());

        Resource resource = change.getCreated().getResource();
        if (resource.isSetInternal()) {
            Internal internal = resource.getInternal();
            source.setResourceInternalDetails(internal.getDetails());
        }

        sourceDao.save(source).ifPresentOrElse(
                dbContractId -> log
                        .info("Source created has been saved, sequenceId={}, sourceId={}", sequenceId, sourceId),
                () -> log.info("Source created bound duplicated, sequenceId={}, sourceId={}", sequenceId, sourceId));
    }

}
