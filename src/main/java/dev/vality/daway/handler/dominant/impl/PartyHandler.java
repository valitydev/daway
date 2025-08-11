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
public class PartyHandler extends AbstractDominantHandler<PartyConfigObject, Party, Long> {

    private final PartyDaoImpl partyDao;

    public PartyHandler(PartyDaoImpl partyDao) {
        this.partyDao = partyDao;
    }

    @Override
    protected DomainObjectDao<Party, Long> getDomainObjectDao() {
        return partyDao;
    }

    @Override
    protected PartyConfigObject getTargetObject() {
        return getDomainObject().getPartyConfig();
    }

    @Override
    protected Long getTargetObjectRefId() {
        return Long.valueOf(getTargetObject().getRef().getId());
    }

    @Override
    protected Long getTargetRefId() {
        return Long.valueOf(getReference().getPartyConfig().getId());
    }

    @Override
    protected boolean acceptDomainObject() {
        return getDomainObject().isSetCategory();
    }

    @Override
    public Party convertToDatabaseObject(PartyConfigObject categoryObject, Long versionId, boolean current) {
        Party party = new Party();
        party.setId(null);
        party.setWtime(null);
        PartyConfig data = categoryObject.getData();
//        party.setPartyId(data.getId());
        party.setName(data.getName());
        party.setContactInfoEmail(data.getContactInfo().getRegistrationEmail());
        List<String> managerContactEmails = data.getContactInfo().getManagerContactEmails();
        party.setManagerContactEmails(StringUtils.collectionToDelimitedString(managerContactEmails, ","));
        party.setBlocking(Blocking.unblocked);
        party.setBlockingUnblockedReason(
                data.getBlock().getBlocked() != null ? data.getBlock().getBlocked().getReason() : "");
        TypeUtil.stringToLocalDateTime(data.getBlock().getBlocked().getSince());
        party.setBlockingBlockedSince(TypeUtil.stringToLocalDateTime(data.getBlock().getBlocked().getSince()));
        party.setSuspension(Suspension.active);
        party.setRevision(0L);
        party.setCurrent(current);
        return party;
    }
}
