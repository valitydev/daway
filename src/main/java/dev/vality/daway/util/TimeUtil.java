package dev.vality.daway.util;

import dev.vality.geck.common.util.TypeUtil;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDateTime;

@UtilityClass
public class TimeUtil {

    public static Pair<LocalDateTime, LocalDateTime> getTimeRange(String eventCreateAt) {
        LocalDateTime toTime = TypeUtil.stringToLocalDateTime(eventCreateAt);
        LocalDateTime fromTime = toTime.minusMonths(1);
        return Pair.of(fromTime, toTime);
    }
}
