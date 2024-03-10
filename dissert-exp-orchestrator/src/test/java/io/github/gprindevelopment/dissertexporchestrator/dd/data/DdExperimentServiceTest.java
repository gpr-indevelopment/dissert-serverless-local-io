package io.github.gprindevelopment.dissertexporchestrator.dd.data;

import io.github.gprindevelopment.dissertexporchestrator.dd.domain.DdOperationStatus;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.SystemName;
import io.github.gprindevelopment.dissertexporchestrator.domain.*;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

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
    @Spy
    private ExperimentFactory experimentFactory = new ExperimentFactory(new ClockService());
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

        when(repository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        DdExperimentEntity saved = experimentService.recordSuccessfulExperiment(
                new SuccessfulExperiment(systemName,
                        resourceTier,
                        rawResponse,
                        rawLatency,
                        rawThroughput,
                        ioSizeBytes,
                        fileSizeBytes,
                        command,
                        operationType));
        assertEquals(systemName, saved.getSystemName());
        assertEquals(resourceTier, saved.getResourceTier());
        assertEquals(command, saved.getCommand());
        assertEquals(ioSizeBytes, saved.getIoSizeBytes());
        assertEquals(fileSizeBytes, saved.getFileSizeBytes());
        assertEquals(operationType, saved.getOperationType());
        assertEquals(DdOperationStatus.SUCCESS, saved.getStatus());
        assertEquals(WeekPeriod.WEEKEND, saved.getWeekPeriod());
        assertEquals(DayOfWeek.SUNDAY, saved.getDayOfWeek());
        assertEquals(TimeOfDay.OFF_HOUR, saved.getTimeOfDay());
        assertNotNull(saved.getOccurredAt());


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
        String rawError = generator.nextObject(String.class);

        when(repository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        DdExperimentEntity saved = experimentService.recordFailedExperiment(
                new FailedExperiment(systemName,
                        resourceTier,
                        rawError,
                        ioSizeBytes,
                        fileSizeBytes,
                        command,
                        operationType));
        assertEquals(systemName, saved.getSystemName());
        assertEquals(resourceTier, saved.getResourceTier());
        assertEquals(command, saved.getCommand());
        assertEquals(ioSizeBytes, saved.getIoSizeBytes());
        assertEquals(fileSizeBytes, saved.getFileSizeBytes());
        assertEquals(operationType, saved.getOperationType());
        assertEquals(DdOperationStatus.FAILURE, saved.getStatus());
        assertEquals(WeekPeriod.WEEKEND, saved.getWeekPeriod());
        assertEquals(DayOfWeek.SUNDAY, saved.getDayOfWeek());
        assertEquals(TimeOfDay.OFF_HOUR, saved.getTimeOfDay());
        assertNotNull(saved.getOccurredAt());

        assertEquals(rawError, saved.getError().getRawError());
        assertEquals(saved, saved.getError().getExperiment());

        assertNull(saved.getResult());
    }

    @Test
    public void Should_set_zero_to_throughput_when_unit_not_resolvable() {
        assertEquals(0, experimentService.extractThroughputKbs("1234 L/s"));
    }

    @Test
    public void Should_correctly_resolve_kBs_throughput_unit() {
        assertEquals(1234, experimentService.extractThroughputKbs("1234 kB/s"));
    }
}