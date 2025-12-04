package dev.vality.daway.dao.dominant.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.dominant.iface.DomainObjectDao;
import dev.vality.daway.domain.Tables;
import dev.vality.daway.domain.tables.pojos.Calendar;
import dev.vality.daway.domain.tables.records.CalendarRecord;
import dev.vality.daway.exception.DaoException;
import org.jooq.Query;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class CalendarDaoImpl extends AbstractGenericDao implements DomainObjectDao<Calendar, Integer> {

    public CalendarDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long save(Calendar calendar) throws DaoException {
        CalendarRecord calendarRecord = getDslContext().newRecord(Tables.CALENDAR, calendar);
        Query query = getDslContext().insertInto(Tables.CALENDAR)
                .set(calendarRecord)
                .returning(Tables.CALENDAR.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeOne(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public void updateNotCurrent(Integer calendarId) throws DaoException {
        Query query = getDslContext().update(Tables.CALENDAR).set(Tables.CALENDAR.CURRENT, false)
                .where(Tables.CALENDAR.CALENDAR_REF_ID.eq(calendarId).and(Tables.CALENDAR.CURRENT));
        executeOne(query);
    }
}
