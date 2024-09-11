package dev.vality.daway.handler.event.stock.impl.partymngmnt.party;

import dev.vality.damsel.domain.PartyContactInfo;
import dev.vality.damsel.payment_processing.AdditionalInfoEffect;
import dev.vality.damsel.payment_processing.AdditionalInfoEffectUnit;
import dev.vality.damsel.payment_processing.ClaimEffect;
import dev.vality.damsel.payment_processing.PartyChange;
import dev.vality.daway.dao.party.iface.PartyDao;
import dev.vality.daway.domain.tables.pojos.Party;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.daway.handler.event.stock.impl.partymngmnt.AbstractClaimChangedHandler;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartyModificationAdditionalInfoHandler extends AbstractClaimChangedHandler {

    public static final String PARTY_ADDITIONAL_INFO_EVENT = "party_additional_info";
    private final PartyDao partyDao;
    private final MachineEventCopyFactory<Party, Integer> partyIntegerMachineEventCopyFactory;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(PartyChange change, MachineEvent event, Integer changeId) {
        long sequenceId = event.getEventId();
        List<ClaimEffect> claimEffects = getClaimStatus(change).getAccepted().getEffects();
        if (claimEffects.stream().anyMatch(ClaimEffect::isSetAdditionalInfoEffect)) {
            List<AdditionalInfoEffect> additionalInfoEffects = claimEffects.stream()
                    .filter(ClaimEffect::isSetAdditionalInfoEffect)
                    .map(ClaimEffect::getAdditionalInfoEffect)
                    .map(AdditionalInfoEffectUnit::getEffect)
                    .toList();
            String partyId = event.getSourceId();
            log.info("Start party additional info effect handling, sequenceId={}, partyId={}, changeId={}", sequenceId, partyId,
                    changeId);
            Party partyOld = partyDao.get(partyId);
            Party partyNew = partyIntegerMachineEventCopyFactory.create(event, sequenceId, changeId, partyOld, null);
            fillPartyAdditionalInfo(additionalInfoEffects, partyNew);
            partyDao.saveWithUpdateCurrent(partyNew, partyOld.getId(), PARTY_ADDITIONAL_INFO_EVENT);
        }
    }

    private static void fillPartyAdditionalInfo(List<AdditionalInfoEffect> additionalInfoEffects, Party partyNew) {
        Optional<String> partyName = additionalInfoEffects.stream()
                .filter(AdditionalInfoEffect::isSetPartyName)
                .map(AdditionalInfoEffect::getPartyName)
                .findFirst();
        partyName.ifPresent(partyNew::setName);
        Optional<String> partyComment = additionalInfoEffects.stream()
                .filter(AdditionalInfoEffect::isSetPartyComment)
                .map(AdditionalInfoEffect::getPartyComment)
                .findFirst();
        partyComment.ifPresent(partyNew::setComment);
        Optional<PartyContactInfo> contactInfo = additionalInfoEffects.stream()
                .filter(AdditionalInfoEffect::isSetContactInfo)
                .map(AdditionalInfoEffect::getContactInfo)
                .findFirst();
        contactInfo.ifPresent(partyContactInfo -> {
            String managerContactEmails = StringUtils.collectionToDelimitedString(
                    partyContactInfo.getManagerContactEmails(), ",");
            partyNew.setManagerContactEmails(managerContactEmails);
        });
    }

}
