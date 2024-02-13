package io.github.gprindevelopment.dissertexporchestrator.dd.data;

import io.github.gprindevelopment.dissertexporchestrator.dd.domain.DdOperationStatus;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.SystemName;
import io.github.gprindevelopment.dissertexporchestrator.domain.*;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.DayOfWeek;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DdExperimentServiceTest {
    @InjectMocks
    private DdExperimentService experimentService;
    @Mock
    private DdExperimentRepository repository;
    @Mock
    private ClockService clockService;
    private final EasyRandom generator = new EasyRandom();

    @Test
    public void Should_record_successful_experiment() {
        String rawResponse = generator.nextObject(String.class);
        String rawLatency = "1.31127 s";
        String rawThroughput = "762 MB/s";
        Long ioSizeBytes = generator.nextLong();
        Long fileSizeBytes = generator.nextLong();
        String command = generator.nextObject(String.class);
        OperationType operationType = OperationType.WRITE;
        SystemName systemName = SystemName.GCF_DD;
        ResourceTier resourceTier = ResourceTier.TIER_1;
        Timestamp occurredAt = new Timestamp(1699801200000L);

        when(clockService.getCurrentTimestamp()).thenReturn(occurredAt);
        when(repository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        DdExperimentEntity saved = experimentService.recordSuccessfulExperiment(
                systemName,
                resourceTier,
                rawResponse,
                rawLatency,
                rawThroughput,
                ioSizeBytes,
                fileSizeBytes,
                command,
                operationType
        );
        assertEquals(systemName, saved.getSystemName());
        assertEquals(resourceTier, saved.getResourceTier());
        assertEquals(command, saved.getCommand());
        assertEquals(ioSizeBytes, saved.getIoSizeBytes());
        assertEquals(fileSizeBytes, saved.getFileSizeBytes());
        assertEquals(operationType, saved.getOperationType());
        assertEquals(DdOperationStatus.SUCCESS, saved.getStatus());
        assertEquals(occurredAt, saved.getOccurredAt());
        assertEquals(WeekPeriod.WEEKEND, saved.getWeekPeriod());
        assertEquals(DayOfWeek.SUNDAY, saved.getDayOfWeek());
        assertEquals(TimeOfDay.OFF_HOUR, saved.getTimeOfDay());

        assertEquals(rawResponse, saved.getResult().getRawResponse());
        assertEquals(rawLatency, saved.getResult().getRawLatency());
        assertEquals(rawThroughput, saved.getResult().getRawThroughput());
        assertEquals(saved, saved.getResult().getExperiment());
        assertEquals(1.31127, saved.getResult().getLatencySeconds());
        assertEquals(762_000, saved.getResult().getThroughputKbPerSecond());

        assertNull(saved.getError());
    }

    @Test
    public void Should_record_failed_experiment() {
        Long ioSizeBytes = generator.nextLong();
        Long fileSizeBytes = generator.nextLong();
        String command = generator.nextObject(String.class);
        OperationType operationType = OperationType.WRITE;
        SystemName systemName = SystemName.GCF_DD;
        ResourceTier resourceTier = ResourceTier.TIER_1;
        Timestamp occurredAt = new Timestamp(1699801200000L);
        String rawError = generator.nextObject(String.class);

        when(clockService.getCurrentTimestamp()).thenReturn(occurredAt);
        when(repository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        DdExperimentEntity saved = experimentService.recordFailedExperiment(
                systemName,
                resourceTier,
                rawError,
                ioSizeBytes,
                fileSizeBytes,
                command,
                operationType
        );
        assertEquals(systemName, saved.getSystemName());
        assertEquals(resourceTier, saved.getResourceTier());
        assertEquals(command, saved.getCommand());
        assertEquals(ioSizeBytes, saved.getIoSizeBytes());
        assertEquals(fileSizeBytes, saved.getFileSizeBytes());
        assertEquals(operationType, saved.getOperationType());
        assertEquals(DdOperationStatus.FAILURE, saved.getStatus());
        assertEquals(occurredAt, saved.getOccurredAt());
        assertEquals(WeekPeriod.WEEKEND, saved.getWeekPeriod());
        assertEquals(DayOfWeek.SUNDAY, saved.getDayOfWeek());
        assertEquals(TimeOfDay.OFF_HOUR, saved.getTimeOfDay());

        assertEquals(rawError, saved.getError().getRawError());
        assertEquals(saved, saved.getError().getExperiment());

        assertNull(saved.getResult());
    }

    /**
     * Teste com todos os parses dessa camada...
     *
     */

}