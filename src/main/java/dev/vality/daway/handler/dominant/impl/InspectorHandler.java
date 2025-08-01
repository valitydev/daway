package dev.vality.daway.handler.dominant.impl;

import dev.vality.damsel.domain.InspectorObject;
import dev.vality.daway.dao.dominant.iface.DomainObjectDao;
import dev.vality.daway.dao.dominant.impl.InspectorDaoImpl;
import dev.vality.daway.domain.tables.pojos.Inspector;
import dev.vality.daway.handler.dominant.AbstractDominantHandler;
import dev.vality.daway.util.JsonUtil;
import org.springframework.stereotype.Component;

@Component
public class InspectorHandler extends AbstractDominantHandler<InspectorObject, Inspector, Integer> {

    private final InspectorDaoImpl inspectorDao;

    public InspectorHandler(InspectorDaoImpl inspectorDao) {
        this.inspectorDao = inspectorDao;
    }

    @Override
    protected DomainObjectDao<Inspector, Integer> getDomainObjectDao() {
        return inspectorDao;
    }

    @Override
    protected InspectorObject getTargetObject() {
        return getDomainObject().getInspector();
    }

    @Override
    protected Integer getTargetObjectRefId() {
        return getTargetObject().getRef().getId();
    }

    @Override
    protected Integer getTargetRefId() {
        return getReference().getInspector().getId();
    }

    @Override
    protected boolean acceptDomainObject() {
        return getDomainObject().isSetInspector();
    }

    @Override
    public Inspector convertToDatabaseObject(InspectorObject inspectorObject, Long versionId, boolean current) {
        Inspector inspector = new Inspector();
        inspector.setVersionId(versionId);
        inspector.setInspectorRefId(getTargetObjectRefId());
        dev.vality.damsel.domain.Inspector data = inspectorObject.getData();
        inspector.setName(data.getName());
        inspector.setDescription(data.getDescription());
        inspector.setProxyRefId(data.getProxy().getRef().getId());
        inspector.setProxyAdditionalJson(JsonUtil.objectToJsonString(data.getProxy().getAdditional()));
        if (data.isSetFallbackRiskScore()) {
            inspector.setFallbackRiskScore(data.getFallbackRiskScore().name());
        }
        inspector.setCurrent(current);
        return inspector;
    }
}
