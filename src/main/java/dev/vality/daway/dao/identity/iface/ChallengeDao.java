package dev.vality.daway.dao.identity.iface;

import dev.vality.dao.GenericDao;
import dev.vality.daway.domain.tables.pojos.Challenge;
import dev.vality.daway.exception.DaoException;

import java.util.Optional;

public interface ChallengeDao extends GenericDao {

    Optional<Long> save(Challenge challenge) throws DaoException;

    Challenge get(String identityId, String challengeId) throws DaoException;

    void updateNotCurrent(String identityId, Long challengeId) throws DaoException;

}
