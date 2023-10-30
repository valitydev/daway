package dev.vality.daway.dao;

import dev.vality.daway.config.PostgresqlSpringBootITest;
import dev.vality.daway.dao.identity.iface.ChallengeDao;
import dev.vality.daway.domain.tables.pojos.Challenge;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@PostgresqlSpringBootITest
public class ChallengeDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ChallengeDao challengeDao;

    @Test
    public void challengeDaoTest() {
        jdbcTemplate.execute("truncate table dw.challenge cascade");
        Challenge challenge = dev.vality.testcontainers.annotations.util.RandomBeans.random(Challenge.class);
        challenge.setCurrent(true);
        Long id = challengeDao.save(challenge).get();
        challenge.setId(id);
        Challenge actual = challengeDao.get(challenge.getIdentityId(), challenge.getChallengeId());
        Assertions.assertEquals(challenge, actual);
        challengeDao.updateNotCurrent(challenge.getIdentityId(), actual.getId());
        Assertions.assertNull(challengeDao.get(challenge.getIdentityId(), challenge.getChallengeId()));

        //check duplicate not error
        challengeDao.save(challenge);
    }

}
