package io.github.gprindevelopment.dissertexporchestrator.domain;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

class ClockServiceTest {

    private final ClockService clockService = new ClockService();

    @Test
    public void Should_return_current_timestamp() {
        Timestamp systemTime = clockService.getCurrentTimestamp();
        assertNotNull(systemTime);
        assertNotEquals(0, systemTime.getTime());
    }
}