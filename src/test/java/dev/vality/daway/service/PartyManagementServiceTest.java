package dev.vality.daway.service;

import dev.vality.damsel.domain.PartyContactInfo;
import dev.vality.damsel.payment_processing.PartyChange;
import dev.vality.damsel.payment_processing.PartyCreated;
import dev.vality.damsel.payment_processing.PartyEventData;
import dev.vality.daway.TestData;
import dev.vality.daway.config.SerializationConfig;
import dev.vality.daway.dao.party.iface.PartyDao;
import dev.vality.daway.domain.tables.pojos.Party;
import dev.vality.daway.factory.machine.event.PartyMachineEventCopyFactoryImpl;
import dev.vality.daway.handler.event.stock.impl.partymngmnt.party.PartyCreatedHandler;
import dev.vality.daway.handler.event.stock.impl.partymngmnt.party.PartyModificationAdditionalInfoHandler;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.machinegun.msgpack.Value;
import dev.vality.sink.common.serialization.impl.PartyEventDataSerializer;
import dev.vality.testcontainers.annotations.util.RandomBeans;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PartyMachineEventCopyFactoryImpl.class,
        PartyCreatedHandler.class, SerializationConfig.class, PartyManagementService.class,
        PartyModificationAdditionalInfoHandler.class})
class PartyManagementServiceTest {

    @Autowired
    PartyManagementService partyManagementService;

    @MockBean
    PartyDao partyDao;

    private final Party party = RandomBeans.random(Party.class);

    @BeforeEach
    public void setUp() {
        when(partyDao.save(any())).thenReturn(Optional.of(1L));
        when(partyDao.get(anyString())).thenReturn(party);
    }

    @Test
    void handleEvents() {
        List<MachineEvent> machineEvents = new ArrayList<>();
        machineEvents.add(createMessage());
        partyManagementService.handleEvents(machineEvents);

        Mockito.verify(partyDao, times(1)).save(any());
    }

    private MachineEvent createMessage() {
        PartyEventData partyEventData = new PartyEventData();
        ArrayList<PartyChange> changes = new ArrayList<>();
        PartyChange partyChange = new PartyChange();
        partyChange.setPartyCreated(new PartyCreated()
                .setContactInfo(new PartyContactInfo()
                        .setRegistrationEmail("test@mail.ru"))
                .setCreatedAt(Instant.now().toString())
                .setId("test"));
        changes.add(partyChange);
        partyEventData.setChanges(changes);
        PartyEventDataSerializer partyEventDataSerializer = new PartyEventDataSerializer();
        Value data = new Value();
        data.setBin(partyEventDataSerializer.serialize(partyEventData));
        MachineEvent message = new MachineEvent();
        message.setCreatedAt(Instant.now().toString());
        message.setEventId(1L);
        message.setSourceNs("sad");
        message.setSourceId("sda");
        message.setData(data);
        return message;
    }

    @Test
    void handlePartyModificationAdditionalInfoEvent() {
        String partyName = "partyName";
        String comment = "comment";
        String email = "test@mail";
        PartyChange partyChange = TestData.createPartyChangeWithPartyAdditionalInfoEffect(partyName, comment, email);
        PartyEventData partyEventData = new PartyEventData();
        partyEventData.setChanges(List.of(partyChange));
        MachineEvent machineEvent = TestData.createPartyEventDataMachineEvent(partyEventData, party.getPartyId());
        List<MachineEvent> machineEvents = new ArrayList<>();
        machineEvents.add(machineEvent);

        partyManagementService.handleEvents(machineEvents);

        Mockito.verify(partyDao, times(1)).saveWithUpdateCurrent(any(), anyLong(), anyString());
    }

}
