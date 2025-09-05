package dev.vality.daway.handler.dominant.impl;

import dev.vality.damsel.domain.DomainObject;
import dev.vality.damsel.domain.TradeBlocObject;
import dev.vality.daway.TestData;
import dev.vality.daway.dao.dominant.iface.DomainObjectDao;
import dev.vality.daway.dao.dominant.impl.TradeBlocDaoImpl;
import dev.vality.daway.domain.tables.pojos.TradeBloc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDateTime;

import static dev.vality.geck.common.util.TypeUtil.temporalToString;
import static org.junit.jupiter.api.Assertions.*;

class TradeBlocHandlerTest {

    @Mock
    private TradeBlocDaoImpl tradeBlocDao;

    private TradeBlocHandler tradeBlocHandler;

    private TradeBlocObject tradeBlocObject;

    @BeforeEach
    void setUp() {
        tradeBlocObject = TestData.buildTradeBlocObject();
        tradeBlocHandler = new TradeBlocHandler(tradeBlocDao);
        tradeBlocHandler.setDomainObject(DomainObject.trade_bloc(tradeBlocObject));
    }

    @Test
    void shouldReturnDao() {
        DomainObjectDao<TradeBloc, String> domainObjectDao = tradeBlocHandler.getDomainObjectDao();
        assertEquals(tradeBlocDao, domainObjectDao);
    }

    @Test
    void shouldReturnTargetObject() {
        TradeBlocObject targetObject = tradeBlocHandler.getTargetObject();
        assertEquals(tradeBlocObject, targetObject);
    }

    @Test
    void shouldReturnTargetObjectRef() {
        String targetObjectRefId = tradeBlocHandler.getTargetObjectRefId();
        assertEquals(tradeBlocObject.getRef().getId(), targetObjectRefId);
    }

    @Test
    void shouldAcceptDomainObject() {
        assertTrue(tradeBlocHandler.acceptDomainObject());
    }

    @Test
    void shouldConvertToDatabaseObject() {
        long versionId = 1L;
        boolean current = false;
        TradeBloc tradeBloc = tradeBlocHandler.convertToDatabaseObject(tradeBlocObject, versionId, current,
                temporalToString(LocalDateTime.now()));
        assertNotNull(tradeBloc);
        assertEquals(tradeBlocObject.getData().getName(), tradeBloc.getName());
        assertEquals(tradeBlocObject.getData().getDescription(), tradeBloc.getDescription());
        assertEquals(versionId, tradeBloc.getVersionId());
        assertEquals(current, tradeBloc.getCurrent());
        assertEquals(tradeBlocObject.getRef().getId(), tradeBloc.getTradeBlocRefId());
    }
}