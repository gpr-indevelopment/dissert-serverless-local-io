package io.github.gprindevelopment.dissertexporchestrator.dd.gcf;

import io.github.gprindevelopment.dissertexporchestrator.dd.domain.CommandRequest;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.DdExpRecordEntity;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.DdExpRecordRepository;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.DdFunctionException;
import io.github.gprindevelopment.dissertexporchestrator.dd.gcf.GcfDdFunctionClient;
import io.github.gprindevelopment.dissertexporchestrator.dd.gcf.GcfDdFunctionService;
import io.github.gprindevelopment.dissertexporchestrator.domain.ClockService;
import io.github.gprindevelopment.dissertexporchestrator.domain.DayOfWeek;
import io.github.gprindevelopment.dissertexporchestrator.domain.OperationType;
import io.github.gprindevelopment.dissertexporchestrator.domain.TimeOfDay;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GcfDdFunctionServiceTest {

    @InjectMocks
    private GcfDdFunctionService gcfDdFunctionService;
    @Mock
    private GcfDdFunctionClient gcfDdFunctionClient;
    @Mock
    private DdExpRecordRepository ddExpRecordRepository;
    @Spy
    private ClockService clockService;

    @Test
    public void Should_successfully_save_exp_record_from_function_call() throws DdFunctionException {
        String expectedFunctionResponse = """
                976+0 records in
                976+0 records out
                999424000 bytes (999 MB, 953 MiB) copied, 0.830883 s, 1.2 GB/s""";
        Long ioSizeBytes = 1_024_000L;
        Long fileSize = 1_000_000_000L;
        String expectedCommand = "if=/dev/zero of=/tmp/file1 bs=1024000 count=976";
        CommandRequest commandRequest = new CommandRequest(expectedCommand);
        when(gcfDdFunctionClient.callFunction(commandRequest)).thenReturn(expectedFunctionResponse);
        when(ddExpRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        DdExpRecordEntity savedEntity = gcfDdFunctionService.collectWriteExpRecord(ioSizeBytes, fileSize);
        assertEquals(savedEntity.getSystemName(), "gcf-dd");
        assertEquals(savedEntity.getRawResponse(), expectedFunctionResponse);
        assertEquals(savedEntity.getCommand(), expectedCommand);
        assertEquals(savedEntity.getOperationType(), OperationType.WRITE);
        assertEquals(savedEntity.getFileSizeBytes(), fileSize);
        assertEquals(savedEntity.getIoSizeBytes(), ioSizeBytes);
        assertEquals(savedEntity.getLatencySeconds(), 0.830883);
        assertEquals(savedEntity.getThroughputKbPerSecond(), 1.2e9);
        assertNotNull(savedEntity.getCollectedAt());
        verify(ddExpRecordRepository).save(any());
    }

    @Test
    public void Should_successfully_post_process_function_response_to_extract_latency_and_throughput() throws DdFunctionException {
        String expectedFunctionResponse = """
                976+0 records in
                976+0 records out
                999424000 bytes (999 MB, 953 MiB) copied, 0.830883 s, 1.2 GB/s""";
        Long ioSizeBytes = 1_024_000L;
        Long fileSize = 1_000_000_000L;
        String expectedCommand = "if=/dev/zero of=/tmp/file1 bs=1024000 count=976";
        CommandRequest commandRequest = new CommandRequest(expectedCommand);
        when(gcfDdFunctionClient.callFunction(commandRequest)).thenReturn(expectedFunctionResponse);
        when(ddExpRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        DdExpRecordEntity savedEntity = gcfDdFunctionService.collectWriteExpRecord(ioSizeBytes, fileSize);
        assertEquals(savedEntity.getRawLatency(), "0.830883 s");
        assertEquals(savedEntity.getRawThroughput(), "1.2 GB/s");
    }

    @Test
    public void Should_throw_dd_exception_when_parsing_response_fails() {
        String expectedFunctionResponse = """
                976+0 records in
                976+0 records out
                999424000 bytes (999 MB) copied, 0.830883 s, 1.2 GB/s""";
        Long ioSizeBytes = 1_024_000L;
        Long fileSize = 1_000_000_000L;
        String expectedCommand = "if=/dev/zero of=/tmp/file1 bs=1024000 count=976";
        CommandRequest commandRequest = new CommandRequest(expectedCommand);
        when(gcfDdFunctionClient.callFunction(commandRequest)).thenReturn(expectedFunctionResponse);
        Throwable thrown = assertThrows(DdFunctionException.class, () -> gcfDdFunctionService.collectWriteExpRecord(ioSizeBytes, fileSize));
        assertTrue(thrown.getMessage().contains(ioSizeBytes.toString()));
        assertTrue(thrown.getMessage().contains(fileSize.toString()));
        assertTrue(thrown.getMessage().contains(expectedCommand));
        assertTrue(thrown.getMessage().contains(expectedFunctionResponse));
        verify(ddExpRecordRepository, never()).save(any());
    }

    @Test
    public void Should_set_zero_to_throughput_when_unit_not_resolvable() throws DdFunctionException {
        String expectedFunctionResponse = """
                976+0 records in
                976+0 records out
                999424000 bytes (999 MB, 953 MiB) copied, 0.830883 s, 1.2 LB/s""";
        Long ioSizeBytes = 1_024_000L;
        Long fileSize = 1_000_000_000L;
        String expectedCommand = "if=/dev/zero of=/tmp/file1 bs=1024000 count=976";
        CommandRequest commandRequest = new CommandRequest(expectedCommand);
        when(gcfDdFunctionClient.callFunction(commandRequest)).thenReturn(expectedFunctionResponse);
        when(ddExpRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        DdExpRecordEntity savedEntity = gcfDdFunctionService.collectWriteExpRecord(ioSizeBytes, fileSize);
        assertEquals(savedEntity.getThroughputKbPerSecond(), 0);
        verify(ddExpRecordRepository).save(any());
    }

    @Test
    public void Should_successfully_populate_time_of_day_as_off_hours() throws DdFunctionException {
        String expectedFunctionResponse = """
                976+0 records in
                976+0 records out
                999424000 bytes (999 MB, 953 MiB) copied, 0.830883 s, 1.2 GB/s""";
        Long ioSizeBytes = 1_024_000L;
        Long fileSize = 1_000_000_000L;
        String expectedCommand = "if=/dev/zero of=/tmp/file1 bs=1024000 count=976";
        CommandRequest commandRequest = new CommandRequest(expectedCommand);
        when(gcfDdFunctionClient.callFunction(commandRequest)).thenReturn(expectedFunctionResponse);
        when(ddExpRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(clockService.getCurrentTimestamp()).thenReturn(new Timestamp(1699493949519L));
        DdExpRecordEntity savedEntity = gcfDdFunctionService.collectWriteExpRecord(ioSizeBytes, fileSize);
        assertEquals(savedEntity.getTimeOfDay(), TimeOfDay.OFF_HOUR);
        verify(ddExpRecordRepository).save(any());
    }

    @Test
    public void Should_successfully_populate_day_of_week_as_weekday() throws DdFunctionException {
        String expectedFunctionResponse = """
                976+0 records in
                976+0 records out
                999424000 bytes (999 MB, 953 MiB) copied, 0.830883 s, 1.2 GB/s""";
        Long ioSizeBytes = 1_024_000L;
        Long fileSize = 1_000_000_000L;
        String expectedCommand = "if=/dev/zero of=/tmp/file1 bs=1024000 count=976";
        CommandRequest commandRequest = new CommandRequest(expectedCommand);
        when(gcfDdFunctionClient.callFunction(commandRequest)).thenReturn(expectedFunctionResponse);
        when(ddExpRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(clockService.getCurrentTimestamp()).thenReturn(new Timestamp(1699493949519L));
        DdExpRecordEntity savedEntity = gcfDdFunctionService.collectWriteExpRecord(ioSizeBytes, fileSize);
        assertEquals(savedEntity.getDayOfWeek(), DayOfWeek.WEEKDAY);
        verify(ddExpRecordRepository).save(any());
    }

    @Test
    public void Should_successfully_populate_time_of_day_as_business_hours() throws DdFunctionException {
        String expectedFunctionResponse = """
                976+0 records in
                976+0 records out
                999424000 bytes (999 MB, 953 MiB) copied, 0.830883 s, 1.2 GB/s""";
        Long ioSizeBytes = 1_024_000L;
        Long fileSize = 1_000_000_000L;
        String expectedCommand = "if=/dev/zero of=/tmp/file1 bs=1024000 count=976";
        CommandRequest commandRequest = new CommandRequest(expectedCommand);
        when(gcfDdFunctionClient.callFunction(commandRequest)).thenReturn(expectedFunctionResponse);
        when(ddExpRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(clockService.getCurrentTimestamp()).thenReturn(new Timestamp(1699455600000L));
        DdExpRecordEntity savedEntity = gcfDdFunctionService.collectWriteExpRecord(ioSizeBytes, fileSize);
        assertEquals(savedEntity.getTimeOfDay(), TimeOfDay.BUSINESS_HOUR);
        verify(ddExpRecordRepository).save(any());
    }

    @Test
    public void Should_successfully_populate_day_of_week_as_weekend() throws DdFunctionException {
        String expectedFunctionResponse = """
                976+0 records in
                976+0 records out
                999424000 bytes (999 MB, 953 MiB) copied, 0.830883 s, 1.2 GB/s""";
        Long ioSizeBytes = 1_024_000L;
        Long fileSize = 1_000_000_000L;
        String expectedCommand = "if=/dev/zero of=/tmp/file1 bs=1024000 count=976";
        CommandRequest commandRequest = new CommandRequest(expectedCommand);
        when(gcfDdFunctionClient.callFunction(commandRequest)).thenReturn(expectedFunctionResponse);
        when(ddExpRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(clockService.getCurrentTimestamp()).thenReturn(new Timestamp(1699801200000L));
        DdExpRecordEntity savedEntity = gcfDdFunctionService.collectWriteExpRecord(ioSizeBytes, fileSize);
        assertEquals(savedEntity.getDayOfWeek(), DayOfWeek.WEEKEND);
        verify(ddExpRecordRepository).save(any());
    }

    @Test
    public void Should_consider_business_hours_in_weekend_as_off_hours() throws DdFunctionException {
        String expectedFunctionResponse = """
                976+0 records in
                976+0 records out
                999424000 bytes (999 MB, 953 MiB) copied, 0.830883 s, 1.2 GB/s""";
        Long ioSizeBytes = 1_024_000L;
        Long fileSize = 1_000_000_000L;
        String expectedCommand = "if=/dev/zero of=/tmp/file1 bs=1024000 count=976";
        CommandRequest commandRequest = new CommandRequest(expectedCommand);
        when(gcfDdFunctionClient.callFunction(commandRequest)).thenReturn(expectedFunctionResponse);
        when(ddExpRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(clockService.getCurrentTimestamp()).thenReturn(new Timestamp(1699801200000L));
        DdExpRecordEntity savedEntity = gcfDdFunctionService.collectWriteExpRecord(ioSizeBytes, fileSize);
        assertEquals(savedEntity.getTimeOfDay(), TimeOfDay.OFF_HOUR);
        assertEquals(savedEntity.getDayOfWeek(), DayOfWeek.WEEKEND);
        verify(ddExpRecordRepository).save(any());
    }
}