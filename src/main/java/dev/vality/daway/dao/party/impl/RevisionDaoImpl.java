package dev.vality.daway.dao.party.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.party.iface.RevisionDao;
import dev.vality.daway.exception.DaoException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class RevisionDaoImpl extends AbstractGenericDao implements RevisionDao {

    public RevisionDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void saveShopsRevision(String partyId, long revision) throws DaoException {
        getNamedParameterJdbcTemplate().update("insert into dw.shop_revision(obj_id, revision) " +
                        "select id, :revision from dw.shop where party_id = :party_id and current",
                new MapSqlParameterSource()
                        .addValue("party_id", partyId)
                        .addValue("revision", revision));
    }

    @Override
    public void saveContractsRevision(String partyId, long revision) throws DaoException {
        getNamedParameterJdbcTemplate().update("insert into dw.contract_revision(obj_id, revision) " +
                        "select id, :revision from dw.contract where party_id = :party_id and current",
                new MapSqlParameterSource()
                        .addValue("party_id", partyId)
                        .addValue("revision", revision));
    }

    @Override
    public void saveContractorsRevision(String partyId, long revision) throws DaoException {
        getNamedParameterJdbcTemplate().update("insert into dw.contractor_revision(obj_id, revision) " +
                        "select id, :revision from dw.contractor where party_id = :party_id and current",
                new MapSqlParameterSource()
                        .addValue("party_id", partyId)
                        .addValue("revision", revision));
    }
}
