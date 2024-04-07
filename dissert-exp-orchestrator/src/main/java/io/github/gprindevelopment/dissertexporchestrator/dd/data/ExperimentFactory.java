package io.github.gprindevelopment.dissertexporchestrator.dd.data;

import io.github.gprindevelopment.dissertexporchestrator.dd.domain.DdExperimentName;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.DdOperationStatus;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.SystemName;
import io.github.gprindevelopment.dissertexporchestrator.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;

@Component
@RequiredArgsConstructor
public class ExperimentFactory {
    private final ClockService clockService;

    public DdExperimentEntity buildExperiment(SuccessfulExperiment successfulExperiment) {
        return buildExperiment(
                successfulExperiment.systemName(),
                successfulExperiment.resourceTier(),
                successfulExperiment.ioSizeBytes(),
                successfulExperiment.fileSizeBytes(),
                successfulExperiment.command(),
                successfulExperiment.operationType(),
                DdOperationStatus.SUCCESS,
                successfulExperiment.experimentName()
        );
    }

    public DdExperimentEntity buildExperiment(FailedExperiment failedExperiment) {
        return buildExperiment(
                failedExperiment.systemName(),
                failedExperiment.resourceTier(),
                failedExperiment.ioSizeBytes(),
                failedExperiment.fileSizeBytes(),
                failedExperiment.command(),
                failedExperiment.operationType(),
                DdOperationStatus.FAILURE,
                failedExperiment.experimentName()
        );
    }

    private DdExperimentEntity buildExperiment(
            SystemName systemName,
            ResourceTier resourceTier,
            Long ioSizeBytes,
            Long fileSizeBytes,
            String command,
            OperationType operationType,
            DdOperationStatus status,
            DdExperimentName experimentName
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
                .experimentName(experimentName)
                .build();
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
