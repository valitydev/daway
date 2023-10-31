package dev.vality.daway.handler.event.stock.impl.identity;

import dev.vality.daway.dao.identity.iface.ChallengeDao;
import dev.vality.daway.domain.tables.pojos.Challenge;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.fistful.identity.*;
import dev.vality.geck.common.util.TBaseUtil;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.geck.filter.Filter;
import dev.vality.geck.filter.PathConditionFilter;
import dev.vality.geck.filter.condition.IsNullCondition;
import dev.vality.geck.filter.rule.PathConditionRule;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdentityChallengeStatusChangedHandler implements IdentityHandler {

    private final ChallengeDao challengeDao;
    private final MachineEventCopyFactory<Challenge, String> challengeMachineEventCopyFactory;

    @Getter
    private Filter filter = new PathConditionFilter(
            new PathConditionRule("change.identity_challenge.payload.status_changed", new IsNullCondition().not()));

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(TimestampedChange timestampedChange, MachineEvent event) {
        Change change = timestampedChange.getChange();
        ChallengeChange challengeChange = change.getIdentityChallenge();
        ChallengeStatus status = challengeChange.getPayload().getStatusChanged();
        long sequenceId = event.getEventId();
        String identityId = event.getSourceId();
        String challengeId = challengeChange.getId();
        log.info("Start identity challenge status changed handling, sequenceId={}, identityId={}, challengeId={}",
                sequenceId, identityId, challengeId);

        var challengeOld = challengeDao.get(identityId, challengeChange.getId());
        var challengeNew = challengeMachineEventCopyFactory
                .create(event, sequenceId, identityId, challengeOld, timestampedChange.getOccuredAt());

        challengeNew.setChallengeId(challengeChange.getId());
        challengeNew.setChallengeStatus(
                TBaseUtil.unionFieldToEnum(status, dev.vality.daway.domain.enums.ChallengeStatus.class));
        if (status.isSetCompleted()) {
            ChallengeCompleted challengeCompleted = status.getCompleted();
            challengeNew.setChallengeResolution(
                    TypeUtil.toEnumField(challengeCompleted.getResolution().toString(), dev.vality.daway.domain.enums.ChallengeResolution.class));
            if (challengeCompleted.isSetValidUntil()) {
                challengeNew.setChallengeValidUntil(TypeUtil.stringToLocalDateTime(challengeCompleted.getValidUntil()));
            }
        }

        challengeDao.save(challengeNew).ifPresentOrElse(
                id -> {
                    challengeDao.updateNotCurrent(identityId, challengeOld.getId());
                    log.info("Identity challenge status have been changed, sequenceId={}, identityId={}", sequenceId,
                            identityId);
                },
                () -> log.info("Identity challenge have been saved, sequenceId={}, identityId={}", sequenceId,
                        identityId));
    }

}
