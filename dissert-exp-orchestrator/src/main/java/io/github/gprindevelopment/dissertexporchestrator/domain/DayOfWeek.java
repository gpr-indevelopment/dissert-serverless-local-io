package io.github.gprindevelopment.dissertexporchestrator.domain;

import java.sql.Timestamp;

public enum DayOfWeek {
    WEEKDAY,WEEKEND;

    public static DayOfWeek from(Timestamp timestamp) {
        DayOfWeek result;
        switch (timestamp.toLocalDateTime().getDayOfWeek()) {
            case SATURDAY, SUNDAY -> result = WEEKEND;
            default -> result = WEEKDAY;
        }
        return result;
    }
}
