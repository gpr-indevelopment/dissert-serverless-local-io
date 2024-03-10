package io.github.gprindevelopment.dissertexporchestrator.dd.data;

import io.github.gprindevelopment.dissertexporchestrator.domain.ClockService;
import io.github.gprindevelopment.dissertexporchestrator.domain.TimeOfDay;
import io.github.gprindevelopment.dissertexporchestrator.domain.WeekPeriod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.DayOfWeek;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExperimentFactoryTest {
    @InjectMocks
    private ExperimentFactory experimentFactory;
    @Mock
    private ClockService clockService;

    @Test
    public void Should_consider_business_hours_in_weekend_as_off_hours() {
        when(clockService.getCurrentTimestamp()).thenReturn(new Timestamp(1699801200000L));
        assertEquals(TimeOfDay.OFF_HOUR, experimentFactory.resolveTimeOfDay());
        assertEquals(WeekPeriod.WEEKEND, experimentFactory.resolveWeekPeriod());
        assertEquals(DayOfWeek.SUNDAY, experimentFactory.resolveDayOfWeek());
    }

    @Test
    public void Should_successfully_populate_week_period_as_weekday() {
        when(clockService.getCurrentTimestamp()).thenReturn(new Timestamp(1699455600000L));
        assertEquals(TimeOfDay.BUSINESS_HOUR, experimentFactory.resolveTimeOfDay());
        assertEquals(WeekPeriod.WEEKDAY, experimentFactory.resolveWeekPeriod());
        assertEquals(DayOfWeek.WEDNESDAY, experimentFactory.resolveDayOfWeek());
    }

}