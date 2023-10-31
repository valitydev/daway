package dev.vality.daway.handler.event.stock.impl.identity;

import dev.vality.daway.dao.identity.iface.ChallengeDao;
import dev.vality.daway.domain.enums.ChallengeStatus;
import dev.vality.daway.domain.tables.pojos.Challenge;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.daway.util.JsonUtil;
import dev.vality.fistful.identity.ChallengeChange;
import dev.vality.fistful.identity.ChallengeChangePayload;
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

import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdentityChallengeCreatedHandler implements IdentityHandler {

    private final ChallengeDao challengeDao;
    private final MachineEventCopyFactory<Challenge, String> challengeMachineEventCopyFactory;

    @Getter
    private Filter filter = new PathConditionFilter(
            new PathConditionRule("change.identity_challenge.payload.created", new IsNullCondition().not()));

    @Override
    public void handle(TimestampedChange timestampedChange, MachineEvent event) {
        Change change = timestampedChange.getChange();
        ChallengeChange challengeChange = change.getIdentityChallenge();
        long sequenceId = event.getEventId();
        String identityId = event.getSourceId();
        String challengeId = challengeChange.getId();
        log.info("Start identity challenge created handling, sequenceId={}, identityId={}, challengeId={}",
                sequenceId, identityId, challengeId);

        Challenge challenge = challengeMachineEventCopyFactory
                .create(event, sequenceId, identityId, timestampedChange.getOccuredAt());

        challenge.setChallengeId(challengeChange.getId());
        ChallengeChangePayload challengePayload = challengeChange.getPayload();
        dev.vality.fistful.identity.Challenge challengePayloadCreated = challengePayload.getCreated();
        challenge.setChallengeClassId(challengePayloadCreated.getCls());
        if (challengePayloadCreated.isSetProofs()) {
            challenge.setProofsJson(JsonUtil.objectToJsonString(
                    challengePayloadCreated.getProofs().stream().map(JsonUtil::thriftBaseToJsonNode)
                            .collect(Collectors.toList())));
        }
        challenge.setChallengeStatus(ChallengeStatus.pending);

        challengeDao.save(challenge).ifPresentOrElse(
                id -> log
                        .info("Start identity challenge have been changed, " +
                                        "sequenceId={}, identityId={}, challengeId={}",
                                sequenceId, identityId, challengeId),
                () -> log.info("Identity challenge have been saved, sequenceId={}, identityId={}, challengeId={}",
                        sequenceId, identityId, challengeId)
        );
    }

}
