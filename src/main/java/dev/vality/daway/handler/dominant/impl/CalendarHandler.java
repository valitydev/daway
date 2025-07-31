package dev.vality.daway.handler.dominant.impl;

import com.fasterxml.jackson.databind.JsonNode;
import dev.vality.damsel.domain.CalendarObject;
import dev.vality.daway.dao.dominant.iface.DomainObjectDao;
import dev.vality.daway.dao.dominant.impl.CalendarDaoImpl;
import dev.vality.daway.domain.tables.pojos.Calendar;
import dev.vality.daway.handler.dominant.AbstractDominantHandler;
import dev.vality.daway.util.JsonUtil;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CalendarHandler extends AbstractDominantHandler<CalendarObject, Calendar, Integer> {

    private final CalendarDaoImpl calendarDao;

    public CalendarHandler(CalendarDaoImpl calendarDao) {
        this.calendarDao = calendarDao;
    }

    @Override
    protected DomainObjectDao<Calendar, Integer> getDomainObjectDao() {
        return calendarDao;
    }

    @Override
    protected CalendarObject getTargetObject() {
        return getDomainObject().getCalendar();
    }

    @Override
    protected Integer getTargetObjectRefId() {
        return getTargetObject().getRef().getId();
    }

    @Override
    protected Integer getTargetRefId() {
        return getReference().getCalendar().getId();
    }

    @Override
    protected boolean acceptDomainObject() {
        return getDomainObject().isSetCalendar();
    }

    @Override
    public Calendar convertToDatabaseObject(CalendarObject calendarObject, Long versionId, boolean current) {
        Calendar calendar = new Calendar();
        calendar.setVersionId(versionId);
        calendar.setCalendarRefId(getTargetObjectRefId());
        dev.vality.damsel.domain.Calendar data = calendarObject.getData();
        calendar.setName(data.getName());
        calendar.setDescription(data.getDescription());
        calendar.setTimezone(data.getTimezone());
        if (data.isSetFirstDayOfWeek()) {
            calendar.setFirstDayOfWeek(data.getFirstDayOfWeek().getValue());
        }
        Map<Integer, Set<JsonNode>> holidaysJsonNodeMap = data.getHolidays().entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> e.getValue()
                                .stream()
                                .map(JsonUtil::thriftBaseToJsonNode)
                                .collect(Collectors.toSet())));
        calendar.setHolidaysJson(JsonUtil.objectToJsonString(holidaysJsonNodeMap));
        calendar.setCurrent(current);
        return calendar;
    }
}
