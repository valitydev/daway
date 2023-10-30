package dev.vality.daway.dao.invoicing.iface;

import dev.vality.daway.exception.DaoException;

import java.util.List;

public interface IdsGeneratorDao {
    List<Long> get(int size) throws DaoException;
}
