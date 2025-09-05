package dev.vality.daway.handler.dominant.impl;

import dev.vality.damsel.domain.ProxyDefinition;
import dev.vality.damsel.domain.ProxyObject;
import dev.vality.daway.dao.dominant.iface.DomainObjectDao;
import dev.vality.daway.dao.dominant.impl.ProxyDaoImpl;
import dev.vality.daway.domain.tables.pojos.Proxy;
import dev.vality.daway.handler.dominant.AbstractDominantHandler;
import dev.vality.geck.common.util.TypeUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ProxyHandler extends AbstractDominantHandler<ProxyObject, Proxy, Integer> {

    private final ProxyDaoImpl proxyDao;

    public ProxyHandler(ProxyDaoImpl proxyDao) {
        this.proxyDao = proxyDao;
    }

    @Override
    protected DomainObjectDao<Proxy, Integer> getDomainObjectDao() {
        return proxyDao;
    }

    @Override
    protected ProxyObject getTargetObject() {
        return getDomainObject().getProxy();
    }

    @Override
    protected Integer getTargetObjectRefId() {
        return getTargetObject().getRef().getId();
    }

    @Override
    protected Integer getTargetRefId() {
        return getReference().getProxy().getId();
    }

    @Override
    protected boolean acceptDomainObject() {
        return getDomainObject().isSetProxy();
    }

    @Override
    public Proxy convertToDatabaseObject(ProxyObject proxyObject, Long versionId, boolean current, String createdAt) {
        Proxy proxy = new Proxy();
        proxy.setVersionId(versionId);
        LocalDateTime createAt = TypeUtil.stringToLocalDateTime(createdAt);
        proxy.setWtime(createAt);
        proxy.setProxyRefId(getTargetObjectRefId());
        ProxyDefinition data = proxyObject.getData();
        proxy.setName(data.getName());
        proxy.setDescription(data.getDescription());
        proxy.setUrl(data.getUrl());
        proxy.setCurrent(current);
        return proxy;
    }
}
