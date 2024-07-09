package dev.vality.daway.handler.event.impl.partymngmnt.party;

import com.zaxxer.hikari.HikariDataSource;
import dev.vality.damsel.payment_processing.EventPayload;
import dev.vality.damsel.payment_processing.PartyChange;
import dev.vality.damsel.payment_processing.PartyRevisionChanged;
import dev.vality.daway.config.PostgresqlSpringBootITest;
import dev.vality.daway.dao.party.iface.*;
import dev.vality.daway.domain.tables.pojos.*;
import dev.vality.daway.handler.event.stock.impl.partymngmnt.party.PartyRevisionChangedHandler;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.sink.common.parser.impl.MachineEventParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@PostgresqlSpringBootITest
@Slf4j
public class PartyRevisionChangedHandlerTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private HikariDataSource dataSource;

    @Autowired
    private PartyRevisionChangedHandler partyRevisionChangedHandler;

    @Autowired
    private ContractDao contractDao;

    @Autowired
    private ShopDao shopDao;

    @Autowired
    private ContractorDao contractorDao;

    @Autowired
    private ContractAdjustmentDao contractAdjustmentDao;

    @Autowired
    private PartyDao partyDao;
    @Mock
    private MachineEventParser eventParser;

    private static final int CNT = 100;

    private static final String PARTY_ID = "partyId";

    @BeforeEach
    public void setUp() {
        log.info("setUp");
        Party party = dev.vality.testcontainers.annotations.util.RandomBeans.random(Party.class, "id", "current", "wtime");
        party.setPartyId(PARTY_ID);
        partyDao.save(party);
        List<Contract> contracts = dev.vality.testcontainers.annotations.util.RandomBeans.randomListOf(CNT, Contract.class, "current");
        List<ContractAdjustment> allAdjustments = new ArrayList<>();
        contracts.forEach(c -> {
            c.setContractId(UUID.randomUUID().toString());
            c.setPartyId(party.getPartyId());
            List<ContractAdjustment> adjustments = dev.vality.testcontainers.annotations.util.RandomBeans.randomListOf(2, ContractAdjustment.class, "id");
            adjustments.forEach(ca -> ca.setCntrctId(c.getId()));
            allAdjustments.addAll(adjustments);
        });
        contracts.forEach(c -> contractDao.save(c));
        contractAdjustmentDao.save(allAdjustments);

        List<Shop> shops = dev.vality.testcontainers.annotations.util.RandomBeans.randomListOf(CNT, Shop.class, "id", "current", "wtime");
        shops.forEach(s -> {
            s.setShopId(UUID.randomUUID().toString());
            s.setPartyId(party.getPartyId());
        });
        shops.forEach(s -> shopDao.save(s));

        List<Contractor> contractors = dev.vality.testcontainers.annotations.util.RandomBeans.randomListOf(CNT, Contractor.class, "id", "current", "wtime");
        contractors.forEach(c -> {
            c.setPartyId(party.getPartyId());
            c.setContractorId(UUID.randomUUID().toString());
        });
        contractors.forEach(c -> contractorDao.save(c));

        log.info("All staff has been saved for partyId={}", party.getPartyId());

    }

    @Test
    public void testPerfomanceHandle() {
        PartyChange change = PartyChange.revision_changed(new PartyRevisionChanged()
                .setTimestamp("2016-03-22T06:12:27Z")
                .setRevision(1L));
        MachineEvent message = new MachineEvent()
                .setSourceId(PARTY_ID)
                .setCreatedAt("2016-03-22T06:12:27Z");

        EventPayload payload = new EventPayload();
        payload.setPartyChanges(List.of(change));

        Mockito.when(eventParser.parse(message)).thenReturn(payload);
        partyRevisionChangedHandler.handle(change, message, 1);

        Assertions.assertEquals(Integer.valueOf(CNT), jdbcTemplate
                .queryForObject("select count(1) from dw.shop_revision", new MapSqlParameterSource(), Integer.class));
        Assertions.assertEquals(Integer.valueOf(CNT), jdbcTemplate
                .queryForObject("select count(1) from dw.contract_revision", new MapSqlParameterSource(),
                        Integer.class));
        Assertions.assertEquals(Integer.valueOf(CNT), jdbcTemplate
                .queryForObject("select count(1) from dw.contractor_revision", new MapSqlParameterSource(),
                        Integer.class));

    }
}
