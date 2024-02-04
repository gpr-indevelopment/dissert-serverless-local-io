package io.github.gprindevelopment.dissertexporchestrator.dd.gcf;

import io.github.gprindevelopment.dissertexporchestrator.dd.domain.*;
import io.github.gprindevelopment.dissertexporchestrator.domain.ClockService;
import io.github.gprindevelopment.dissertexporchestrator.domain.OperationType;
import io.github.gprindevelopment.dissertexporchestrator.gcp.GcfNotFoundException;
import io.github.gprindevelopment.dissertexporchestrator.gcp.GcfResourceTier;
import io.github.gprindevelopment.dissertexporchestrator.gcp.GcfService;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private GcfDdFunctionProps gcfDdFunctionProps;
    @Mock
    private GcfService gcfService;
    @Mock
    private DdExpRecordRepository ddExpRecordRepository;
    @Spy
    private ClockService clockService;
    private final EasyRandom generator = new EasyRandom();

    @Test
    public void Should_successfully_save_exp_record_from_write_function_call() throws DdFunctionException {
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
        assertEquals(savedEntity.getSystemName(), SystemName.GCF_DD);
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
    public void Should_successfully_save_exp_record_from_read_function_call() throws DdFunctionException {
        String expectedFunctionResponse = """
                953+1 records in
                953+1 records out
                999424000 bytes (999 MB, 953 MiB) copied, 0.296427 s, 3.4 GB/s""";
        Long ioSizeBytes = 1_024_000L;
        String expectedCommand = "if=/tmp/file1 of=/dev/null bs=1024000";
        CommandRequest commandRequest = new CommandRequest(expectedCommand);
        when(gcfDdFunctionClient.callFunction(commandRequest)).thenReturn(expectedFunctionResponse);
        when(ddExpRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        DdExpRecordEntity savedEntity = gcfDdFunctionService.collectReadExpRecord(ioSizeBytes);
        assertEquals(savedEntity.getSystemName(), SystemName.GCF_DD);
        assertEquals(savedEntity.getRawResponse(), expectedFunctionResponse);
        assertEquals(savedEntity.getCommand(), expectedCommand);
        assertEquals(savedEntity.getOperationType(), OperationType.READ);
        assertNull(savedEntity.getFileSizeBytes());
        assertEquals(savedEntity.getIoSizeBytes(), ioSizeBytes);
        assertEquals(savedEntity.getLatencySeconds(), 0.296427);
        assertEquals(savedEntity.getThroughputKbPerSecond(), 3.4e9);
        assertNotNull(savedEntity.getCollectedAt());
        verify(ddExpRecordRepository).save(any());
    }

    @Test
    public void Should_successfully_set_function_resources() throws GcfNotFoundException {
        String functionName = generator.nextObject(String.class);
        GcfResourceTier tier = GcfResourceTier.TIER_2;

        when(gcfDdFunctionProps.name()).thenReturn(functionName);

        gcfDdFunctionService.setFunctionResources(tier);
        verify(gcfService).setFunctionResources(functionName, tier);
    }

    @Test
    public void Should_map_to_runtime_exception_if_function_cannot_be_found() throws GcfNotFoundException {
        String functionName = generator.nextObject(String.class);
        GcfResourceTier tier = GcfResourceTier.TIER_2;

        when(gcfService.setFunctionResources(functionName, tier)).thenThrow(GcfNotFoundException.class);
        when(gcfDdFunctionProps.name()).thenReturn(functionName);

        assertThrows(DdFunctionException.class, () -> gcfDdFunctionService.setFunctionResources(tier));
        verify(gcfService).setFunctionResources(functionName, tier);
    }
}