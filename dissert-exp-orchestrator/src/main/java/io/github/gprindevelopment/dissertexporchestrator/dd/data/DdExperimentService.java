package io.github.gprindevelopment.dissertexporchestrator.dd.data;

import io.github.gprindevelopment.dissertexporchestrator.dd.domain.DdOperationStatus;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.SystemName;
import io.github.gprindevelopment.dissertexporchestrator.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;

@Service
@Slf4j
@RequiredArgsConstructor
public class DdExperimentService {
    private final DdExperimentRepository repository;
    private final ClockService clockService;

    public DdExperimentEntity recordSuccessfulExperiment(
            SystemName systemName,
            ResourceTier resourceTier,
            String rawResponse,
            String rawLatency,
            String rawThroughput,
            Long ioSizeBytes,
            Long fileSizeBytes,
            String command,
            OperationType operationType) {
        DdExperimentEntity experiment = buildExperiment(
                systemName,
                resourceTier,
                ioSizeBytes,
                fileSizeBytes,
                command,
                operationType,
                DdOperationStatus.SUCCESS
        );
        DdExperimentResultEntity result = DdExperimentResultEntity
                .builder()
                .rawThroughput(rawThroughput)
                .rawLatency(rawLatency)
                .rawResponse(rawResponse)
                .latencySeconds(extractLatency(rawLatency))
                .throughputKbPerSecond(extractThroughputKbs(rawThroughput))
                .experiment(experiment)
                .build();
        experiment.setResult(result);
        return repository.save(experiment);
    }

    public DdExperimentEntity recordFailedExperiment(
            SystemName systemName,
            ResourceTier resourceTier,
            String rawError,
            Long ioSizeBytes,
            Long fileSizeBytes,
            String command,
            OperationType operationType) {
        DdExperimentEntity experiment = buildExperiment(
                systemName,
                resourceTier,
                ioSizeBytes,
                fileSizeBytes,
                command,
                operationType,
                DdOperationStatus.FAILURE
        );
        DdExperimentErrorEntity error = DdExperimentErrorEntity
                .builder()
                .experiment(experiment)
                .rawError(rawError)
                .build();
        experiment.setError(error);
        return repository.save(experiment);
    }

    private DdExperimentEntity buildExperiment(
            SystemName systemName,
            ResourceTier resourceTier,
            Long ioSizeBytes,
            Long fileSizeBytes,
            String command,
            OperationType operationType,
            DdOperationStatus status

    ) {
        return DdExperimentEntity
                .builder()
                .occurredAt(clockService.getCurrentTimestamp())
                .status(status)
                .systemName(systemName)
                .command(command)
                .ioSizeBytes(ioSizeBytes)
                .fileSizeBytes(fileSizeBytes)
                .operationType(operationType)
                .resourceTier(resourceTier)
                .dayOfWeek(resolveDayOfWeek())
                .weekPeriod(resolveWeekPeriod())
                .timeOfDay(resolveTimeOfDay())
                .build();
    }

    protected Double extractThroughputKbs(String rawThroughput) {
        String[] splitRawThroughput = rawThroughput.split("\s");
        Double value = Double.parseDouble(splitRawThroughput[0]);
        String unit = splitRawThroughput[1];
        double throughputBytes = value * resolveMultiplierFromUnit(unit);
        return throughputBytes/1e3;
    }

    private Double resolveMultiplierFromUnit(String unit) {
        double multiplier;
        switch (unit) {
            case "GB/s" -> multiplier = 1e9;
            case "MB/s" -> multiplier = 1e6;
            case "kB/s" -> multiplier = 1e3;
            default -> {
                multiplier = 0;
                log.warn("Unknown throughput unit: {}. Multiplier is set to {}.", unit, multiplier);
            }
        }
        return multiplier;
    }

    private Double extractLatency(String rawLatency) {
        return Double.parseDouble(rawLatency.split("\s")[0]);
    }

    protected WeekPeriod resolveWeekPeriod() {
        return WeekPeriod.from(resolveDayOfWeek());
    }

    protected DayOfWeek resolveDayOfWeek() {
        return clockService.getCurrentTimestamp().toLocalDateTime().getDayOfWeek();
    }

    protected TimeOfDay resolveTimeOfDay() {
        return TimeOfDay.from(clockService.getCurrentTimestamp(), resolveDayOfWeek());
    }
}
