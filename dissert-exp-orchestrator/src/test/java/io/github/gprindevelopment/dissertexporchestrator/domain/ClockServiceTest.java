package io.github.gprindevelopment.dissertexporchestrator.domain;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ClockServiceTest {

    private final ClockService clockService = new ClockService();

    @Test
    public void Should_return_current_timestamp() {
        Timestamp systemTime = clockService.getCurrentTimestamp();
        assertNotNull(systemTime);
        assertNotEquals(0, systemTime.getTime());
    }

    @Test
    public void Should_wait_a_second_without_errors() {
        clockService.wait(TimeUnit.SECONDS, 1);
    }
}