package dev.vality.daway.handler.dominant.impl;

import dev.vality.damsel.domain.TradeBlocObject;
import dev.vality.daway.dao.dominant.iface.DomainObjectDao;
import dev.vality.daway.dao.dominant.impl.TradeBlocDaoImpl;
import dev.vality.daway.domain.tables.pojos.TradeBloc;
import dev.vality.daway.handler.dominant.AbstractDominantHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TradeBlocHandler extends AbstractDominantHandler<TradeBlocObject, TradeBloc, String> {

    private final TradeBlocDaoImpl tradeBlocDao;

    @Override
    protected DomainObjectDao<TradeBloc, String> getDomainObjectDao() {
        return tradeBlocDao;
    }

    @Override
    protected TradeBlocObject getTargetObject() {
        return getDomainObject().getTradeBloc();
    }

    @Override
    protected String getTargetObjectRefId() {
        return getTargetObject().getRef().getId();
    }

    @Override
    protected String getTargetRefId() {
        return getReference().getTradeBloc().getId();
    }

    @Override
    protected boolean acceptDomainObject() {
        return getDomainObject().isSetTradeBloc();
    }

    @Override
    public TradeBloc convertToDatabaseObject(TradeBlocObject object, Long versionId, boolean current) {
        TradeBloc tradeBloc = new TradeBloc();
        tradeBloc.setVersionId(versionId);
        tradeBloc.setTradeBlocRefId(getTargetObjectRefId());
        dev.vality.damsel.domain.TradeBloc data = object.getData();
        tradeBloc.setName(data.getName());
        tradeBloc.setDescription(data.getDescription());
        tradeBloc.setCurrent(current);
        return tradeBloc;
    }
}
