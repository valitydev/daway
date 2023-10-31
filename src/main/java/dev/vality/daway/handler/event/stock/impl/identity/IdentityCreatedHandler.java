package dev.vality.daway.handler.event.stock.impl.identity;

import com.fasterxml.jackson.databind.JsonNode;
import dev.vality.daway.dao.identity.iface.IdentityDao;
import dev.vality.daway.domain.tables.pojos.Identity;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.daway.util.JsonUtil;
import dev.vality.fistful.identity.Change;
import dev.vality.fistful.identity.TimestampedChange;
import dev.vality.geck.filter.Filter;
import dev.vality.geck.filter.PathConditionFilter;
import dev.vality.geck.filter.condition.IsNullCondition;
import dev.vality.geck.filter.rule.PathConditionRule;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdentityCreatedHandler implements IdentityHandler {

    private final IdentityDao identityDao;
    private final MachineEventCopyFactory<Identity, String> identityMachineEventCopyFactory;

    @Getter
    private Filter filter =
            new PathConditionFilter(new PathConditionRule("change.created", new IsNullCondition().not()));

    @Override
    public void handle(TimestampedChange timestampedChange, MachineEvent event) {
        long sequenceId = event.getEventId();
        String identityId = event.getSourceId();
        log.info("Start identity created handling, sequenceId={}, identityId={}", sequenceId, identityId);

        Identity identity =
                identityMachineEventCopyFactory.create(event, sequenceId, identityId, timestampedChange.getOccuredAt());

        Change change = timestampedChange.getChange();
        dev.vality.fistful.identity.Identity changeCreated = change.getCreated();
        identity.setPartyId(changeCreated.getParty());
        identity.setPartyContractId(changeCreated.getContract());
        identity.setIdentityProviderId(changeCreated.getProvider());
        identity.setExternalId(changeCreated.getExternalId());
        if (changeCreated.isSetMetadata()) {
            Map<String, JsonNode> jsonNodeMap = changeCreated.getMetadata().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> JsonUtil.thriftBaseToJsonNode(e.getValue())));
            identity.setContextJson(JsonUtil.objectToJsonString(jsonNodeMap));
        }
        identityDao.save(identity).ifPresentOrElse(
                id -> log.info("Identity haven been saved, sequenceId={}, identityId={}", sequenceId, identityId),
                () -> log
                        .info("Identity created bound duplicated, sequenceId={}, identityId={}", sequenceId, identityId)
        );
    }

}
