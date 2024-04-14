package io.github.gprindevelopment.dissertexporchestrator.dd.common;

import com.google.common.base.Throwables;
import io.github.gprindevelopment.dissertexporchestrator.dd.data.DdExperimentEntity;
import io.github.gprindevelopment.dissertexporchestrator.dd.data.DdExperimentService;
import io.github.gprindevelopment.dissertexporchestrator.dd.data.FailedExperiment;
import io.github.gprindevelopment.dissertexporchestrator.dd.data.SuccessfulExperiment;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.CommandRequest;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.DdExperimentName;
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
        return collectExpRecord(ioSizeBytes,
                fileSizeBytes,
                buildZeroWriteCommand(ioSizeBytes, fileSizeBytes),
                OperationType.WRITE,
                DdExperimentName.DEV_ZERO_WRITE);
    }

    public DdExperimentEntity collectURandomDirectWriteExpRecord(IoSizeTier ioSizeTier, FileSizeTier fileSizeTier) {
        return collectExpRecord(ioSizeTier.getIoSizeBytes(),
                fileSizeTier.getFileSizeBytes(),
                buildURandomDirectWriteCommand(ioSizeTier, fileSizeTier),
                OperationType.WRITE,
                DdExperimentName.DIRECT_URANDOM_WRITE);
    }

    public DdExperimentEntity collectURandomWriteExpRecord(IoSizeTier ioSizeTier, FileSizeTier fileSizeTier) {
        return collectExpRecord(ioSizeTier.getIoSizeBytes(),
                fileSizeTier.getFileSizeBytes(),
                buildURandomWriteCommand(ioSizeTier, fileSizeTier),
                OperationType.WRITE,
                DdExperimentName.DIRECT_URANDOM_WRITE);
    }

    /**
     * Requires that a file with absolute path /tmp/file1 exists prior to the read action.
     * Will read the entirety of the existing file using the provided ioSizeTier
     * Will persist experiment considering read file with fileSizeTier
     *
     * @param ioSizeTier
     * @param fileSizeTier
     * @return the result of the read experiment
     */
    public DdExperimentEntity collectReadExpRecord(IoSizeTier ioSizeTier, FileSizeTier fileSizeTier) {
        return collectExpRecord(ioSizeTier.getIoSizeBytes(),
                fileSizeTier.getFileSizeBytes(),
                buildReadCommand(ioSizeTier),
                OperationType.READ,
                DdExperimentName.DIRECT_READ);
    }

    private DdExperimentEntity collectExpRecord(
            Long ioSizeBytes,
            Long fileSizeBytes,
            String command,
            OperationType operationType,
            DdExperimentName experimentName) {
        if (Objects.isNull(currentResourceTier)) {
            log.info("Current resource tier is null. Will calibrate and default to TIER 1.");
            setFunctionResources(ResourceTier.TIER_1);
        }
        CommandRequest commandRequest = new CommandRequest(command);
        try {
            String rawResponse = callFunction(commandRequest);
            log.debug("Function called successfully. Will parse and save record");
            SuccessfulExperiment successfulExperiment = new SuccessfulExperiment(getSystemName(),
                    currentResourceTier,
                    rawResponse,
                    extractRawLatency(rawResponse),
                    extractRawThroughput(rawResponse),
                    ioSizeBytes,
                    fileSizeBytes,
                    command,
                    operationType,
                    experimentName);
            return experimentService.recordSuccessfulExperiment(successfulExperiment);
        } catch (Exception ex) {
            return saveOperationError(
                    ioSizeBytes,
                    fileSizeBytes,
                    command,
                    operationType,
                    experimentName,
                    ex
            );
        }
    }

    private DdExperimentEntity saveOperationError(
            Long ioSizeBytes,
            Long fileSizeBytes,
            String command,
            OperationType operationType,
            DdExperimentName experimentName,
            Exception error
    ) {
        log.info("Error found. Will record a failed experiment.");
        DdExperimentEntity saved = experimentService.recordFailedExperiment(
                new FailedExperiment(getSystemName(),
                        currentResourceTier,
                        Throwables.getStackTraceAsString(error),
                        ioSizeBytes,
                        fileSizeBytes,
                        command,
                        operationType,
                        experimentName));
        log.info("Successfully persisted DdExperimentEntity as failure: {}", saved);
        return saved;
    }

    private String buildZeroWriteCommand(Long ioSizeBytes, Long fileSizeBytes) {
        return String.format("if=/dev/zero of=/tmp/file1 bs=%d count=%d", ioSizeBytes, fileSizeBytes / ioSizeBytes);
    }

    private String buildURandomDirectWriteCommand(IoSizeTier ioSizeTier, FileSizeTier fileSizeTier) {
        return String.format("oflag=direct if=/dev/urandom of=/tmp/file1 bs=%s count=%d", ioSizeTier.getStringNotationBytes(), fileSizeTier.getFileSizeBytes() / ioSizeTier.getIoSizeBytes());
    }

    private String buildURandomWriteCommand(IoSizeTier ioSizeTier, FileSizeTier fileSizeTier) {
        return String.format("if=/dev/urandom of=/tmp/file1 bs=%s count=%d", ioSizeTier.getStringNotationBytes(), fileSizeTier.getFileSizeBytes() / ioSizeTier.getIoSizeBytes());
    }

    private String buildReadCommand(IoSizeTier ioSizeTier) {
        return String.format("iflag=direct if=/tmp/file1 of=/dev/null bs=%s", ioSizeTier.getStringNotationBytes());
    }
}
