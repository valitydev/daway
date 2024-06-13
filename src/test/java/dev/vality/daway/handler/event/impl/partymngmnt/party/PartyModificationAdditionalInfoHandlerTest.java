package dev.vality.daway.handler.event.impl.partymngmnt.party;

import dev.vality.damsel.payment_processing.PartyChange;
import dev.vality.damsel.payment_processing.PartyEventData;
import dev.vality.daway.TestData;
import dev.vality.daway.config.PostgresqlJooqSpringBootITest;
import dev.vality.daway.dao.party.impl.PartyDaoImpl;
import dev.vality.daway.domain.tables.pojos.Party;
import dev.vality.daway.domain.tables.records.PartyRecord;
import dev.vality.daway.factory.machine.event.PartyMachineEventCopyFactoryImpl;
import dev.vality.daway.handler.event.stock.impl.partymngmnt.party.PartyModificationAdditionalInfoHandler;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.testcontainers.annotations.util.RandomBeans;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static dev.vality.daway.domain.tables.Party.PARTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@PostgresqlJooqSpringBootITest
@ContextConfiguration(classes = {PartyDaoImpl.class, PartyMachineEventCopyFactoryImpl.class,
        PartyModificationAdditionalInfoHandler.class,})
class PartyModificationAdditionalInfoHandlerTest {

    @Autowired
    PartyModificationAdditionalInfoHandler handler;

    @Autowired
    DSLContext dslContext;

    @BeforeEach
    void setUp() {
        dslContext.deleteFrom(PARTY).execute();
    }

    @Test
    void handle() {
        String partyName = "partyName";
        String comment = "comment";
        String email = "test@mail";
        Party party = RandomBeans.random(Party.class);
        party.setCurrent(Boolean.TRUE);
        dslContext.insertInto(PARTY)
                .set(dslContext.newRecord(PARTY, party))
                .execute();
        PartyChange partyChange = TestData.createPartyChangeWithPartyModificationAdditionalInfo(partyName, comment, email);
        PartyEventData partyEventData = new PartyEventData();
        partyEventData.setChanges(List.of(partyChange));
        MachineEvent machineEvent = TestData.createPartyEventDataMachineEvent(partyEventData, party.getPartyId());

        handler.handle(partyChange, machineEvent, 1);

        Result<PartyRecord> record = dslContext.fetch(PARTY, PARTY.CURRENT.eq(Boolean.TRUE));
        PartyRecord partyRecord = record.get(0);
        assertNotNull(partyRecord);
        assertEquals(partyName, partyRecord.getName());
        assertEquals(comment, partyRecord.getComment());
        assertThat(partyRecord.getManagerContactEmails(), containsString(email));
    }
}
