package dev.vality.daway.handler.event.stock.impl.partymngmnt.party;

import dev.vality.damsel.payment_processing.AdditionalInfoModificationUnit;
import dev.vality.damsel.payment_processing.PartyChange;
import dev.vality.damsel.payment_processing.PartyModification;
import dev.vality.daway.dao.party.iface.PartyDao;
import dev.vality.daway.domain.tables.pojos.Party;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.daway.handler.event.stock.impl.partymngmnt.AbstractPartyModificationHandler;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartyModificationAdditionalInfoHandler extends AbstractPartyModificationHandler {


    public static final String PARTY_MODIFICATION_ADDITIONAL_INFO_EVENT = "party_modification_additional_info";
    private final PartyDao partyDao;
    private final MachineEventCopyFactory<Party, Integer> partyIntegerMachineEventCopyFactory;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(PartyChange change, MachineEvent event, Integer changeId) {
        long sequenceId = event.getEventId();
        List<PartyModification> partyModifications = getPartyModification(change);
        if (partyModifications.stream().anyMatch(PartyModification::isSetAdditionalInfoModification)) {
            AdditionalInfoModificationUnit additionalInfoModification = partyModifications.stream()
                    .filter(PartyModification::isSetAdditionalInfoModification)
                    .findFirst()
                    .map(PartyModification::getAdditionalInfoModification)
                    .orElseThrow(() -> new IllegalArgumentException(""));
            String partyId = event.getSourceId();
            log.info("Start party modification additional info handling, sequenceId={}, partyId={}, changeId={}", sequenceId, partyId,
                    changeId);
            Party partyOld = partyDao.get(partyId);
            Party partyNew = partyIntegerMachineEventCopyFactory.create(event, sequenceId, changeId, partyOld, null);

            partyNew.setName(additionalInfoModification.getPartyName());
            partyNew.setComment(additionalInfoModification.getComment());
            String managerContactEmails = StringUtils.collectionToDelimitedString(
                    additionalInfoModification.getManagerContactEmails(), ",");
            partyNew.setManagerContactEmails(managerContactEmails);

            partyDao.saveWithUpdateCurrent(partyNew, partyOld.getId(), PARTY_MODIFICATION_ADDITIONAL_INFO_EVENT);
        }

    }

}
