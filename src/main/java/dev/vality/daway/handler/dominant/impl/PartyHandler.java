package dev.vality.daway.handler.dominant.impl;

import dev.vality.damsel.domain.PartyConfig;
import dev.vality.damsel.domain.PartyConfigObject;
import dev.vality.daway.dao.dominant.iface.DomainObjectDao;
import dev.vality.daway.dao.party.impl.PartyDaoImpl;
import dev.vality.daway.domain.enums.Blocking;
import dev.vality.daway.domain.enums.Suspension;
import dev.vality.daway.domain.tables.pojos.Party;
import dev.vality.daway.handler.dominant.AbstractDominantHandler;
import dev.vality.geck.common.util.TypeUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PartyHandler extends AbstractDominantHandler<PartyConfigObject, Party, String> {

    private final PartyDaoImpl partyDao;

    public PartyHandler(PartyDaoImpl partyDao) {
        this.partyDao = partyDao;
    }

    @Override
    protected DomainObjectDao<Party, String> getDomainObjectDao() {
        return partyDao;
    }

    @Override
    protected PartyConfigObject getTargetObject() {
        return getDomainObject().getPartyConfig();
    }

    @Override
    protected String getTargetObjectRefId() {
        return getTargetObject().getRef().getId();
    }

    @Override
    protected String getTargetRefId() {
        return getReference().getPartyConfig().getId();
    }

    @Override
    protected boolean acceptDomainObject() {
        return getDomainObject().isSetPartyConfig();
    }

    @Override
    public Party convertToDatabaseObject(PartyConfigObject partyConfigObject, Long versionId, boolean current) {
        Party party = new Party();
        party.setId(null);
        party.setWtime(null);
        PartyConfig data = partyConfigObject.getData();
        party.setPartyId(partyConfigObject.getRef().id);
        party.setName(data.getName());
        party.setContactInfoEmail(data.getContactInfo().getRegistrationEmail());
        List<String> managerContactEmails = data.getContactInfo().getManagerContactEmails();
        party.setManagerContactEmails(StringUtils.collectionToDelimitedString(managerContactEmails, ","));
        party.setBlocking(Blocking.unblocked);
        party.setBlockingUnblockedReason(getReason(data));
        party.setBlockingBlockedSince(getTime(data));
        party.setSuspension(Suspension.active);
        party.setRevision(0L);
        party.setCurrent(current);
        return party;
    }

    private static String getReason(PartyConfig data) {
        if (data.getBlock().isSetUnblocked()) {
            return data.getBlock().getUnblocked().getReason();
        } else if (data.getBlock().isSetBlocked()) {
            return data.getBlock().getBlocked().getReason();
        } else {
            return "";
        }
    }

    private static LocalDateTime getTime(PartyConfig data) {
        if (data.getBlock().isSetUnblocked()) {
            return TypeUtil.stringToLocalDateTime(data.getBlock().getBlocked().getSince());
        } else if (data.getBlock().isSetBlocked()) {
            return TypeUtil.stringToLocalDateTime(data.getBlock().getUnblocked().getSince());
        } else {
            return null;
        }
    }
}
