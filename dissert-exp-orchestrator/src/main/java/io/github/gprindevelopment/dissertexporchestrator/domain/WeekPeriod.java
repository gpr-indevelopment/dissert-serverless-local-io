package io.github.gprindevelopment.dissertexporchestrator.domain;

import java.sql.Timestamp;
import java.time.DayOfWeek;

public enum WeekPeriod {

    WEEKDAY,WEEKEND;

    public static WeekPeriod from(Timestamp timestamp) {
        return from(timestamp.toLocalDateTime().getDayOfWeek());
    }

    public static WeekPeriod from(DayOfWeek dayOfWeek) {
        WeekPeriod result;
        switch (dayOfWeek) {
            case SATURDAY, SUNDAY -> result = WEEKEND;
            default -> result = WEEKDAY;
        }
        return result;
    }
}
