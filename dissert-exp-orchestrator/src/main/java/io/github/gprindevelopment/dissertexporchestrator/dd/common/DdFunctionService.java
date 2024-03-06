package io.github.gprindevelopment.dissertexporchestrator.dd.common;

import com.google.common.base.Throwables;
import io.github.gprindevelopment.dissertexporchestrator.dd.data.DdExperimentEntity;
import io.github.gprindevelopment.dissertexporchestrator.dd.data.DdExperimentService;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.CommandRequest;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.SystemName;
import io.github.gprindevelopment.dissertexporchestrator.domain.FileSizeTier;
import io.github.gprindevelopment.dissertexporchestrator.domain.IoSizeTier;
import io.github.gprindevelopment.dissertexporchestrator.domain.OperationType;
import io.github.gprindevelopment.dissertexporchestrator.domain.ResourceTier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public abstract class DdFunctionService {

    private final DdExperimentService experimentService;
    protected ResourceTier currentResourceTier;

    protected abstract String callFunction(CommandRequest commandRequest);
    protected abstract String extractRawLatency(String rawResponse);
    protected abstract String extractRawThroughput(String rawResponse);
    protected abstract SystemName getSystemName();
    protected abstract void callSetFunctionResources(ResourceTier resourceTier);

    public void setFunctionResources(ResourceTier resourceTier) {
        callSetFunctionResources(resourceTier);
        currentResourceTier = resourceTier;
    }
    public DdExperimentEntity collectZeroWriteExpRecord(IoSizeTier ioSizeTier, FileSizeTier fileSizeTier) {
        Long ioSizeBytes = ioSizeTier.getIoSizeBytes();
        Long fileSizeBytes = fileSizeTier.getFileSizeBytes();
        return collectExpRecord(ioSizeBytes, fileSizeBytes, buildZeroWriteCommand(ioSizeBytes, fileSizeBytes), OperationType.WRITE);
    }

    public DdExperimentEntity collectURandomWriteExpRecord(IoSizeTier ioSizeTier, FileSizeTier fileSizeTier) {
        Long ioSizeBytes = ioSizeTier.getIoSizeBytes();
        Long fileSizeBytes = fileSizeTier.getFileSizeBytes();
        return collectExpRecord(ioSizeBytes, fileSizeBytes, buildURandomWriteCommand(ioSizeBytes, fileSizeBytes), OperationType.WRITE);
    }

    public DdExperimentEntity collectReadExpRecord(IoSizeTier ioSizeTier) {
        Long ioSizeBytes = ioSizeTier.getIoSizeBytes();
        return collectExpRecord(ioSizeBytes, null, buildReadCommand(ioSizeBytes), OperationType.READ);
    }

    private DdExperimentEntity collectExpRecord(
            Long ioSizeBytes,
            Long fileSizeBytes,
            String command,
            OperationType operationType) {
        if (Objects.isNull(currentResourceTier)) {
            log.info("Current resource tier is null. Will calibrate and default to TIER 1.");
            setFunctionResources(ResourceTier.TIER_1);
        }
        CommandRequest commandRequest = new CommandRequest(command);
        try {
            String rawResponse = callFunction(commandRequest);
            log.debug("Function called successfully. Will parse and save record");
            return experimentService.recordSuccessfulExperiment(
                    getSystemName(),
                    currentResourceTier,
                    rawResponse,
                    extractRawLatency(rawResponse),
                    extractRawThroughput(rawResponse),
                    ioSizeBytes,
                    fileSizeBytes,
                    command,
                    operationType
            );
        } catch (Exception ex) {
            return saveOperationError(
                    ioSizeBytes,
                    fileSizeBytes,
                    command,
                    operationType,
                    ex
            );
        }
    }

    private DdExperimentEntity saveOperationError(
            Long ioSizeBytes,
            Long fileSizeBytes,
            String command,
            OperationType operationType,
            Exception error
    ) {
        log.info("Error found. Will record a failed experiment.");
        DdExperimentEntity saved = experimentService.recordFailedExperiment(
                getSystemName(),
                currentResourceTier,
                Throwables.getStackTraceAsString(error),
                ioSizeBytes,
                fileSizeBytes,
                command,
                operationType
        );
        log.info("Successfully persisted DdExperimentEntity as failure: {}", saved);
        return saved;
    }

    private String buildZeroWriteCommand(Long ioSizeBytes, Long fileSizeBytes) {
        return String.format("if=/dev/zero of=/tmp/file1 bs=%d count=%d", ioSizeBytes, fileSizeBytes/ioSizeBytes);
    }

    private String buildURandomWriteCommand(Long ioSizeBytes, Long fileSizeBytes) {
        return String.format("if=/dev/urandom of=/tmp/file1 bs=%d count=%d", ioSizeBytes, fileSizeBytes/ioSizeBytes);
    }

    private String buildReadCommand(Long ioSizeBytes) {
        return String.format("if=/tmp/file1 of=/dev/null bs=%d", ioSizeBytes);
    }
}
