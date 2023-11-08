package io.github.gprindevelopment.dissertexporchestrator.dd.common;

import io.github.gprindevelopment.dissertexporchestrator.dd.domain.DdFunctionException;
import io.github.gprindevelopment.dissertexporchestrator.domain.OperationType;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.CommandRequest;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.DdExpRecordEntity;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.DdExpRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;

@Slf4j
@RequiredArgsConstructor
public abstract class DdFunctionService {

    private final DdExpRecordRepository ddExpRecordRepository;
    protected abstract String callFunction(CommandRequest commandRequest);

    public DdExpRecordEntity collectWriteExpRecord(Long ioSizeBytes, Long fileSizeBytes) throws DdFunctionException {
        String command = buildWriteCommand(ioSizeBytes, fileSizeBytes);
        CommandRequest commandRequest = new CommandRequest(command);
        String rawResponse = callFunction(commandRequest);
        return parseAndSaveRecord(rawResponse, ioSizeBytes, fileSizeBytes, command);
    }

    private DdExpRecordEntity parseAndSaveRecord(String rawResponse, Long ioSizeBytes, Long fileSizeBytes, String command) throws DdFunctionException {
        try {
            DdExpRecordEntity ddExpRecordEntity = DdExpRecordEntity
                    .builder()
                    .rawResponse(rawResponse)
                    .collectedAt(new Timestamp(System.currentTimeMillis()))
                    .systemName("gcf-dd")
                    .command(command)
                    .operationType(OperationType.WRITE)
                    .ioSizeBytes(ioSizeBytes)
                    .fileSizeBytes(fileSizeBytes)
                    .rawLatency(extractRawLatency(rawResponse))
                    .rawThroughput(extractRawThroughput(rawResponse))
                    .build();
            ddExpRecordEntity = ddExpRecordRepository.save(ddExpRecordEntity);
            log.info("Persisted write experimental record: {}", ddExpRecordEntity);
            return ddExpRecordEntity;
        } catch (Exception ex) {
            String message = String.format("Unable to build and save record with ioSizeBytes: %d, fileSizeBytes: %d and command: %s for rawResponse: %s",
                    ioSizeBytes,
                    fileSizeBytes,
                    command,
                    rawResponse);
            throw new DdFunctionException(message, ex);
        }
    }

    protected abstract String extractRawLatency(String rawResponse);

    protected abstract String extractRawThroughput(String rawResponse);

    private String buildWriteCommand(Long ioSizeBytes, Long fileSizeBytes) {
        return String.format("if=/dev/zero of=/tmp/file1 bs=%d count=%d", ioSizeBytes, fileSizeBytes/ioSizeBytes);
    }
}
