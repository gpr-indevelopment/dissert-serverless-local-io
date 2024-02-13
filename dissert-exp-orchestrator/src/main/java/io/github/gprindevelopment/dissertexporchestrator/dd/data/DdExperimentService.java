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
                .throughputKbPerSecond(extractThroughput(rawThroughput))
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

    private Double extractThroughput(String rawThroughput) {
        String[] splitRawThroughput = rawThroughput.split("\s");
        Double value = Double.parseDouble(splitRawThroughput[0]);
        String unit = splitRawThroughput[1];
        return value * resolveMultiplierFromUnit(unit);
    }

    private Double resolveMultiplierFromUnit(String unit) {
        double multiplier = 1.0;
        switch (unit) {
            case "GB/s":
                multiplier = 1e6;
                break;
            case "MB/s":
                multiplier = 1e3;
                break;
            default:
                log.warn("Unknown throughput unit: {}. Multiplier is set to {}.", unit, multiplier);
                break;
        }
        return multiplier;
    }

    private Double extractLatency(String rawLatency) {
        return Double.parseDouble(rawLatency.split("\s")[0]);
    }

    private WeekPeriod resolveWeekPeriod() {
        return WeekPeriod.from(resolveDayOfWeek());
    }

    private DayOfWeek resolveDayOfWeek() {
        return clockService.getCurrentTimestamp().toLocalDateTime().getDayOfWeek();
    }

    private TimeOfDay resolveTimeOfDay() {
        return TimeOfDay.from(clockService.getCurrentTimestamp(), resolveDayOfWeek());
    }
}
