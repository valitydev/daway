package dev.vality.daway.handler.dominant.impl;

import dev.vality.damsel.domain.CountryObject;
import dev.vality.damsel.domain.TradeBlocRef;
import dev.vality.daway.dao.dominant.iface.DomainObjectDao;
import dev.vality.daway.dao.dominant.impl.CountryDaoImpl;
import dev.vality.daway.domain.tables.pojos.Country;
import dev.vality.daway.handler.dominant.AbstractDominantHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CountryHandler extends AbstractDominantHandler<CountryObject, Country, String> {

    private final CountryDaoImpl countryDao;

    @Override
    protected DomainObjectDao<Country, String> getDomainObjectDao() {
        return countryDao;
    }

    @Override
    protected CountryObject getTargetObject() {
        return getDomainObject().getCountry();
    }

    @Override
    protected String getTargetObjectRefId() {
        return getTargetObject().getRef().getId().name();
    }

    @Override
    protected String getTargetRefId() {
        return getReference().getCountry().getId().name();
    }

    @Override
    protected boolean acceptDomainObject() {
        return getDomainObject().isSetCountry();
    }

    @Override
    public Country convertToDatabaseObject(CountryObject object, Long versionId, boolean current) {
        Country country = new Country();
        country.setVersionId(versionId);
        country.setCountryRefId(getTargetObjectRefId());
        dev.vality.damsel.domain.Country data = object.getData();
        country.setName(data.getName());
        String[] tradeBlocs = data.getTradeBlocs().stream()
                .map(TradeBlocRef::getId)
                .toArray(String[]::new);
        country.setTradeBloc(tradeBlocs);
        country.setCurrent(current);
        return country;
    }
}
