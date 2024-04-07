package io.github.gprindevelopment.dissertexporchestrator.dd.data;

import io.github.gprindevelopment.dissertexporchestrator.dd.domain.DdExperimentName;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.SystemName;
import io.github.gprindevelopment.dissertexporchestrator.domain.OperationType;
import io.github.gprindevelopment.dissertexporchestrator.domain.ResourceTier;

public record FailedExperiment(SystemName systemName, ResourceTier resourceTier, String rawError, Long ioSizeBytes,
                               Long fileSizeBytes, String command, OperationType operationType, DdExperimentName experimentName) {
}