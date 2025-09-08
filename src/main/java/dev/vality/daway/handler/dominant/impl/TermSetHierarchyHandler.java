package dev.vality.daway.handler.dominant.impl;

import dev.vality.damsel.domain.TermSetHierarchyObject;
import dev.vality.daway.dao.dominant.iface.DomainObjectDao;
import dev.vality.daway.dao.dominant.impl.TermSetHierarchyDaoImpl;
import dev.vality.daway.domain.tables.pojos.TermSetHierarchy;
import dev.vality.daway.handler.dominant.AbstractDominantHandler;
import dev.vality.daway.util.JsonUtil;
import dev.vality.geck.common.util.TypeUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Component
public class TermSetHierarchyHandler
        extends AbstractDominantHandler<TermSetHierarchyObject, TermSetHierarchy, Integer> {

    private final TermSetHierarchyDaoImpl termSetHierarchyDao;

    public TermSetHierarchyHandler(TermSetHierarchyDaoImpl termSetHierarchyDao) {
        this.termSetHierarchyDao = termSetHierarchyDao;
    }

    @Override
    protected DomainObjectDao<TermSetHierarchy, Integer> getDomainObjectDao() {
        return termSetHierarchyDao;
    }

    @Override
    protected TermSetHierarchyObject getTargetObject() {
        return getDomainObject().getTermSetHierarchy();
    }

    @Override
    protected Integer getTargetObjectRefId() {
        return getTargetObject().getRef().getId();
    }

    @Override
    protected Integer getTargetRefId() {
        return getReference().getTermSetHierarchy().getId();
    }

    @Override
    protected boolean acceptDomainObject() {
        return getDomainObject().isSetTermSetHierarchy();
    }

    @Override
    public TermSetHierarchy convertToDatabaseObject(TermSetHierarchyObject termSetHierarchyObject, Long versionId,
                                                    boolean current, String createdAt) {
        TermSetHierarchy termSetHierarchy = new TermSetHierarchy();
        termSetHierarchy.setVersionId(versionId);
        termSetHierarchy.setWtime(null);
        termSetHierarchy.setTermSetHierarchyRefId(getTargetObjectRefId());
        dev.vality.damsel.domain.TermSetHierarchy data = termSetHierarchyObject.getData();
        termSetHierarchy.setName(data.getName());
        termSetHierarchy.setDescription(data.getDescription());
        if (data.isSetParentTerms()) {
            termSetHierarchy.setParentTermsRefId(data.getParentTerms().getId());
        }
        termSetHierarchy.setTermSetsJson(JsonUtil.objectToJsonString(JsonUtil.thriftBaseToJsonNode(data.getTermSet())));
        termSetHierarchy.setCurrent(current);
        return termSetHierarchy;
    }
}
