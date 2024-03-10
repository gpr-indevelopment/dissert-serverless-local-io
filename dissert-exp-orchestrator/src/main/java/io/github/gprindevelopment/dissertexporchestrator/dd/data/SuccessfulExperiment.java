package io.github.gprindevelopment.dissertexporchestrator.dd.data;

import io.github.gprindevelopment.dissertexporchestrator.dd.domain.SystemName;
import io.github.gprindevelopment.dissertexporchestrator.domain.OperationType;
import io.github.gprindevelopment.dissertexporchestrator.domain.ResourceTier;

public record SuccessfulExperiment(SystemName systemName, ResourceTier resourceTier, String rawResponse,
                                   String rawLatency, String rawThroughput, Long ioSizeBytes, Long fileSizeBytes,
                                   String command, OperationType operationType) {
}