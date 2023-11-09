package io.github.gprindevelopment.dissertexporchestrator.domain;

import java.sql.Timestamp;
import java.time.LocalTime;

public enum TimeOfDay {
    BUSINESS_HOUR,OFF_HOUR;

    public static TimeOfDay from(Timestamp timestamp, DayOfWeek dayOfWeek) {
        return from(timestamp.toLocalDateTime().toLocalTime(), dayOfWeek);
    }

    public static TimeOfDay from(LocalTime localTime, DayOfWeek dayOfWeek) {
        if (dayOfWeek.equals(DayOfWeek.WEEKEND)) {
            return OFF_HOUR;
        }
        LocalTime nineAm = LocalTime.of(9, 0);
        LocalTime sixPm = LocalTime.of(18,0);
        if (localTime.isAfter(nineAm) && localTime.isBefore(sixPm)) {
            return BUSINESS_HOUR;
        }
        return OFF_HOUR;
    }
}
