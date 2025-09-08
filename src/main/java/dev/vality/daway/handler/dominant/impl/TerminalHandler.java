package dev.vality.daway.handler.dominant.impl;

import dev.vality.damsel.domain.TerminalObject;
import dev.vality.daway.dao.dominant.iface.DomainObjectDao;
import dev.vality.daway.dao.dominant.impl.TerminalDaoImpl;
import dev.vality.daway.domain.tables.pojos.Terminal;
import dev.vality.daway.handler.dominant.AbstractDominantHandler;
import dev.vality.daway.util.JsonUtil;
import dev.vality.geck.common.util.TypeUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TerminalHandler extends AbstractDominantHandler<TerminalObject, Terminal, Integer> {

    private final TerminalDaoImpl terminalDao;

    public TerminalHandler(TerminalDaoImpl terminalDao) {
        this.terminalDao = terminalDao;
    }

    @Override
    protected DomainObjectDao<Terminal, Integer> getDomainObjectDao() {
        return terminalDao;
    }

    @Override
    protected TerminalObject getTargetObject() {
        return getDomainObject().getTerminal();
    }

    @Override
    protected Integer getTargetObjectRefId() {
        return getTargetObject().getRef().getId();
    }

    @Override
    protected Integer getTargetRefId() {
        return getReference().getTerminal().getId();
    }

    @Override
    protected boolean acceptDomainObject() {
        return getDomainObject().isSetTerminal();
    }

    @Override
    public Terminal convertToDatabaseObject(TerminalObject terminalObject, Long versionId, boolean current,
                                            String createdAt) {
        Terminal terminal = new Terminal();
        terminal.setVersionId(versionId);
        terminal.setWtime(null);
        terminal.setTerminalRefId(getTargetObjectRefId());
        dev.vality.damsel.domain.Terminal data = terminalObject.getData();
        terminal.setName(data.getName());
        terminal.setDescription(data.getDescription());
        if (data.isSetRiskCoverage()) {
            terminal.setRiskCoverage(data.getRiskCoverage().name());
        }
        if (data.isSetTerms()) {
            terminal.setTermsJson(JsonUtil.thriftBaseToJsonString(data.getTerms()));
        }
        terminal.setExternalTerminalId(data.getExternalTerminalId());
        terminal.setExternalMerchantId(data.getExternalMerchantId());
        terminal.setMcc(data.getMcc());
        if (data.isSetProviderRef()) {
            terminal.setTerminalProviderRefId(data.getProviderRef().getId());
        }
        terminal.setCurrent(current);
        return terminal;
    }
}
