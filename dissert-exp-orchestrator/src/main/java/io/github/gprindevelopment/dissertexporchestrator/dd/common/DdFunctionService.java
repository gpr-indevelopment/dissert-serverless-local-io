package io.github.gprindevelopment.dissertexporchestrator.dd.common;

import io.github.gprindevelopment.dissertexporchestrator.dd.domain.*;
import io.github.gprindevelopment.dissertexporchestrator.domain.ClockService;
import io.github.gprindevelopment.dissertexporchestrator.domain.OperationType;
import io.github.gprindevelopment.dissertexporchestrator.domain.TimeOfDay;
import io.github.gprindevelopment.dissertexporchestrator.domain.WeekPeriod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;

@Slf4j
@RequiredArgsConstructor
public abstract class DdFunctionService {

    private final DdExpRecordRepository ddExpRecordRepository;
    private final ClockService clockService;
    protected abstract String callFunction(CommandRequest commandRequest);

    public DdExpRecordEntity collectWriteExpRecord(Long ioSizeBytes, Long fileSizeBytes) {
        String command = buildWriteCommand(ioSizeBytes, fileSizeBytes);
        CommandRequest commandRequest = new CommandRequest(command);
        String rawResponse = callFunction(commandRequest);
        return parseAndSaveRecord(rawResponse, ioSizeBytes, fileSizeBytes, command, OperationType.WRITE);
    }

    public DdExpRecordEntity collectReadExpRecord(Long ioSizeBytes) {
        String command = buildReadCommand(ioSizeBytes);
        CommandRequest commandRequest = new CommandRequest(command);
        String rawResponse = callFunction(commandRequest);
        return parseAndSaveRecord(rawResponse, ioSizeBytes, null, command, OperationType.READ);
    }

    protected abstract String extractRawLatency(String rawResponse);

    protected abstract String extractRawThroughput(String rawResponse);

    protected abstract SystemName getSystemName();

    private DdExpRecordEntity parseAndSaveRecord(String rawResponse, Long ioSizeBytes, Long fileSizeBytes, String command, OperationType operationType) {
        try {
            DdExpRecordEntity ddExpRecordEntity = DdExpRecordEntity
                    .builder()
                    .rawResponse(rawResponse)
                    .collectedAt(clockService.getCurrentTimestamp())
                    .systemName(getSystemName())
                    .command(command)
                    .operationType(operationType)
                    .ioSizeBytes(ioSizeBytes)
                    .fileSizeBytes(fileSizeBytes)
                    .rawLatency(extractRawLatency(rawResponse))
                    .rawThroughput(extractRawThroughput(rawResponse))
                    .latencySeconds(extractLatency(rawResponse))
                    .throughputKbPerSecond(extractThroughput(rawResponse))
                    .timeOfDay(resolveTimeOfDay())
                    .dayOfWeek(resolveDayOfWeek())
                    .weekPeriod(resolveWeekPeriod())
                    .build();
            ddExpRecordEntity = ddExpRecordRepository.save(ddExpRecordEntity);
            log.info("Persisted write experimental record: {}", ddExpRecordEntity);
            return ddExpRecordEntity;
        } catch (Exception ex) {
            String message = String.format("Unable to build and save record with ioSizeBytes: %d, fileSizeBytes: %d, operationType: %s and command: %s for rawResponse: %s",
                    ioSizeBytes,
                    fileSizeBytes,
                    operationType,
                    command,
                    rawResponse);
            throw new DdFunctionException(message, ex);
        }
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

    private String buildWriteCommand(Long ioSizeBytes, Long fileSizeBytes) {
        return String.format("if=/dev/zero of=/tmp/file1 bs=%d count=%d", ioSizeBytes, fileSizeBytes/ioSizeBytes);
    }

    private String buildReadCommand(Long ioSizeBytes) {
        return String.format("if=/tmp/file1 of=/dev/null bs=%d", ioSizeBytes);
    }
}
