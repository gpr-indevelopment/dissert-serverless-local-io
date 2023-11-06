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
    private final ExpRecordRepository expRecordRepository;

    public ExpRecordEntity collectWriteExpRecord(Long ioSize, Long blockCount) {
        String command = buildWriteCommand(ioSize, blockCount);
        CommandRequest commandRequest = new CommandRequest(command);
        String rawResponse = gcfDdFunctionClient.callFunction(commandRequest);
        ExpRecordEntity expRecordEntity = ExpRecordEntity
                .builder()
                .rawContent(rawResponse)
                .collectedAt(new Timestamp(System.currentTimeMillis()))
                .systemName("gcf-dd")
                .command(command)
                .operationType(OperationType.WRITE)
                .build();
        expRecordEntity = expRecordRepository.save(expRecordEntity);
        log.info("Persisted write experimental record: {}", expRecordEntity);
        return expRecordEntity;
    }

    private String buildWriteCommand(Long ioSize, Long blockCount) {
        return String.format("if=/dev/zero of=/tmp/file1 bs=%d count=%d", ioSize, blockCount);
    }
}
