package dev.vality.daway.handler.event.stock.impl.recurrent.payment.tool;

import dev.vality.damsel.payment_processing.RecurrentPaymentToolChange;
import dev.vality.daway.dao.recurrent.payment.tool.iface.RecurrentPaymentToolDao;
import dev.vality.daway.domain.enums.RecurrentPaymentToolStatus;
import dev.vality.daway.domain.tables.pojos.RecurrentPaymentTool;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
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
public class RecurrentPaymentToolHasAcquiredHandler implements RecurrentPaymentToolHandler {

    private final RecurrentPaymentToolDao recurrentPaymentToolDao;
    private final MachineEventCopyFactory<RecurrentPaymentTool, Integer> recurrentPaymentToolCopyFactory;

    @Getter
    private final Filter filter = new PathConditionFilter(
            new PathConditionRule("rec_payment_tool_acquired", new IsNullCondition().not()));

    @Override
    public void handle(RecurrentPaymentToolChange change, MachineEvent event, Integer changeId) {
        long sequenceId = event.getEventId();
        log.info("Start recurrent payment tool acquired handling, sourceId={}, sequenceId={}, changeId={}",
                event.getSourceId(), sequenceId, changeId);
        final RecurrentPaymentTool recurrentPaymentToolOld = recurrentPaymentToolDao.get(event.getSourceId());
        RecurrentPaymentTool recurrentPaymentToolNew =
                recurrentPaymentToolCopyFactory.create(event, sequenceId, changeId, recurrentPaymentToolOld, null);
        recurrentPaymentToolNew.setStatus(RecurrentPaymentToolStatus.acquired);
        recurrentPaymentToolNew.setRecToken(change.getRecPaymentToolAcquired().getToken());
        recurrentPaymentToolDao.save(recurrentPaymentToolNew).ifPresentOrElse(
                id -> {
                    recurrentPaymentToolDao.updateNotCurrent(recurrentPaymentToolOld.getId());
                    log.info("End recurrent payment tool acquired handling, sourceId={}, sequenceId={}, changeId={}",
                            event.getSourceId(), sequenceId, changeId);
                },
                () -> log.info("End recurrent payment tool acquired bound duplicated, sequenceId={}, changeId={}",
                        sequenceId, changeId));
    }

}
