package io.github.gprindevelopment.dissertexporchestrator.dd.common;

import io.github.gprindevelopment.dissertexporchestrator.dd.domain.DdFunctionException;
import io.github.gprindevelopment.dissertexporchestrator.domain.ClockService;
import io.github.gprindevelopment.dissertexporchestrator.domain.DayOfWeek;
import io.github.gprindevelopment.dissertexporchestrator.domain.OperationType;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.CommandRequest;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.DdExpRecordEntity;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.DdExpRecordRepository;
import io.github.gprindevelopment.dissertexporchestrator.domain.TimeOfDay;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@RequiredArgsConstructor
public abstract class DdFunctionService {

    private final DdExpRecordRepository ddExpRecordRepository;
    private final ClockService clockService;
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
                    .collectedAt(clockService.getCurrentTimestamp())
                    .systemName("gcf-dd")
                    .command(command)
                    .operationType(OperationType.WRITE)
                    .ioSizeBytes(ioSizeBytes)
                    .fileSizeBytes(fileSizeBytes)
                    .rawLatency(extractRawLatency(rawResponse))
                    .rawThroughput(extractRawThroughput(rawResponse))
                    .latencySeconds(extractLatency(rawResponse))
                    .throughputKbPerSecond(extractThroughput(rawResponse))
                    .timeOfDay(resolveTimeOfDay())
                    .dayOfWeek(resolveDayOfWeek())
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

    private DayOfWeek resolveDayOfWeek() {
        return DayOfWeek.from(clockService.getCurrentTimestamp());
    }

    private TimeOfDay resolveTimeOfDay() {
        return TimeOfDay.from(clockService.getCurrentTimestamp(), resolveDayOfWeek());
    }

    private Double extractThroughput(String rawResponse) {
        String rawThroughput = extractRawThroughput(rawResponse);
        String[] splitRawThroughput = rawThroughput.split("\s");
        Double value = Double.parseDouble(splitRawThroughput[0]);
        String unit = splitRawThroughput[1];
        return value * resolveMultiplierFromUnit(unit);
    }

    private Double resolveMultiplierFromUnit(String unit) {
        double multiplier = 1.0;
        switch (unit) {
            case "GB/s":
                multiplier = 1e9;
                break;
            case "MB/s":
                multiplier = 1e6;
                break;
            default:
                log.warn("Unknown throughput unit: {}. Multiplier is set to 0.", unit);
                multiplier = 0;
                break;
        }
        return multiplier;
    }

    private Double extractLatency(String rawResponse) {
        String rawLatency = extractRawLatency(rawResponse);
        return Double.parseDouble(rawLatency.split("\s")[0]);
    }

    protected abstract String extractRawLatency(String rawResponse);

    protected abstract String extractRawThroughput(String rawResponse);

    private String buildWriteCommand(Long ioSizeBytes, Long fileSizeBytes) {
        return String.format("if=/dev/zero of=/tmp/file1 bs=%d count=%d", ioSizeBytes, fileSizeBytes/ioSizeBytes);
    }
}
