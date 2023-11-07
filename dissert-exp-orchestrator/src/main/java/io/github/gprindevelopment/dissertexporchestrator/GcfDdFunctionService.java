package io.github.gprindevelopment.dissertexporchestrator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Component
@Slf4j
@RequiredArgsConstructor
public class GcfDdFunctionService {

    private final GcfDdFunctionClient gcfDdFunctionClient;
    private final DdExpRecordRepository ddExpRecordRepository;

    public DdExpRecordEntity collectWriteExpRecord(Long ioSizeBytes, Long fileSizeBytes) {
        String command = buildWriteCommand(ioSizeBytes, fileSizeBytes);
        CommandRequest commandRequest = new CommandRequest(command);
        String rawResponse = gcfDdFunctionClient.callFunction(commandRequest);
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
    }

    private String extractRawLatency(String rawResponse) {
        return rawResponse.split(",")[2].trim();
    }

    private String extractRawThroughput(String rawResponse) {
        return rawResponse.split(",")[3].trim();
    }

    private String buildWriteCommand(Long ioSizeBytes, Long fileSizeBytes) {
        return String.format("if=/dev/zero of=/tmp/file1 bs=%d count=%d", ioSizeBytes, fileSizeBytes/ioSizeBytes);
    }
}
