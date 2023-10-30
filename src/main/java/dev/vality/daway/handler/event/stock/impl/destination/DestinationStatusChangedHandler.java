package dev.vality.daway.handler.event.stock.impl.destination;

import dev.vality.daway.dao.destination.iface.DestinationDao;
import dev.vality.daway.domain.enums.DestinationStatus;
import dev.vality.daway.domain.tables.pojos.Destination;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.fistful.destination.Change;
import dev.vality.fistful.destination.Status;
import dev.vality.fistful.destination.TimestampedChange;
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
public class DestinationStatusChangedHandler implements DestinationHandler {

    private final DestinationDao destinationDao;
    private final MachineEventCopyFactory<Destination, String> destinationMachineEventCopyFactory;

    @Getter
    private final Filter filter = new PathConditionFilter(
            new PathConditionRule("change.status.changed", new IsNullCondition().not()));

    @Override
    public void handle(TimestampedChange timestampedChange, MachineEvent event) {
        Change change = timestampedChange.getChange();
        Status status = change.getStatus().getChanged();
        long sequenceId = event.getEventId();
        String destinationId = event.getSourceId();
        log.info("Start destination status changed handling, sequenceId={}, destinationId={}", sequenceId,
                destinationId);

        final Destination destinationOld = destinationDao.get(destinationId);
        Destination destinationNew = destinationMachineEventCopyFactory
                .create(event, sequenceId, destinationId, destinationOld, timestampedChange.getOccuredAt());

        destinationNew.setDestinationStatus(TBaseUtil.unionFieldToEnum(status, DestinationStatus.class));

        destinationDao.save(destinationNew).ifPresentOrElse(
                id -> {
                    destinationDao.updateNotCurrent(destinationOld.getId());
                    log.info("Destination status have been changed, sequenceId={}, destinationId={}", sequenceId,
                            destinationId);
                },
                () -> log
                        .info("Destination have been saved, sequenceId={}, destinationId={}", sequenceId, destinationId)
        );
    }

}
