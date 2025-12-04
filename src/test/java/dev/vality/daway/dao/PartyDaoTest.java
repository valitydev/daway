package dev.vality.daway.dao;

import dev.vality.daway.config.PostgresqlJooqSpringBootITest;
import dev.vality.daway.dao.dominant.impl.PartyDaoImpl;
import dev.vality.daway.domain.tables.pojos.Party;
import dev.vality.daway.domain.tables.records.PartyRecord;
import dev.vality.testcontainers.annotations.util.RandomBeans;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static dev.vality.daway.domain.tables.Party.PARTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@PostgresqlJooqSpringBootITest
@ContextConfiguration(classes = {PartyDaoImpl.class})
class PartyDaoTest {

    @Autowired
    PartyDaoImpl partyDao;

    @Autowired
    DSLContext dslContext;

    @BeforeEach
    void setUp() {
        dslContext.deleteFrom(PARTY).execute();
    }

    @Test
    void save() {
        Party party = RandomBeans.random(Party.class);

        partyDao.save(party);

        PartyRecord partyRecord = dslContext.fetchAny(PARTY);

        assertNotNull(partyRecord);
        assertEquals(party.getPartyId(), partyRecord.getPartyId());
        assertEquals(party.getDominantVersionId(), partyRecord.getDominantVersionId());
        assertEquals(party.getName(), partyRecord.getName());

    }
}