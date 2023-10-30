package dev.vality.daway.handler.dominant.impl;

import dev.vality.damsel.domain.CountryObject;
import dev.vality.damsel.domain.DomainObject;
import dev.vality.damsel.domain.TradeBlocRef;
import dev.vality.daway.TestData;
import dev.vality.daway.dao.dominant.iface.DomainObjectDao;
import dev.vality.daway.dao.dominant.impl.CountryDaoImpl;
import dev.vality.daway.domain.tables.pojos.Country;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;

class CountryHandlerTest {

    @Mock
    private CountryDaoImpl countryDao;

    private CountryHandler countryHandler;

    private CountryObject countryObject;

    @BeforeEach
    void setUp() {
        countryObject = TestData.buildCountryObject();
        countryHandler = new CountryHandler(countryDao);
        countryHandler.setDomainObject(DomainObject.country(countryObject));
    }

    @Test
    void shouldReturnDao() {
        DomainObjectDao<Country, String> domainObjectDao = countryHandler.getDomainObjectDao();
        assertEquals(countryDao, domainObjectDao);
    }

    @Test
    void shouldReturnTargetObject() {
        CountryObject targetObject = countryHandler.getTargetObject();
        assertEquals(countryObject, targetObject);
    }

    @Test
    void shouldReturnTargetObjectRef() {
        String targetObjectRefId = countryHandler.getTargetObjectRefId();
        assertEquals(countryObject.getRef().getId().name(), targetObjectRefId);
    }

    @Test
    void shouldAcceptDomainObject() {
        assertTrue(countryHandler.acceptDomainObject());
    }

    @Test
    void shouldConvertToDatabaseObject() {
        long versionId = 1L;
        boolean current = false;
        Country country = countryHandler.convertToDatabaseObject(countryObject, versionId, current);
        assertNotNull(country);
        assertEquals(countryObject.getData().getName(), country.getName());
        assertEquals(versionId, country.getVersionId());
        assertEquals(current, country.getCurrent());
        assertEquals(countryObject.getRef().getId().name(), country.getCountryRefId());
        assertArrayEquals(
                countryObject.getData().getTradeBlocs().stream().map(TradeBlocRef::getId).toArray(String[]::new),
                country.getTradeBloc());

    }
}