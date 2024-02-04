package io.github.gprindevelopment.dissertexporchestrator.dd.gcf;

import io.github.gprindevelopment.dissertexporchestrator.dd.domain.*;
import io.github.gprindevelopment.dissertexporchestrator.domain.*;
import io.github.gprindevelopment.dissertexporchestrator.gcp.GcfNotFoundException;
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
    public void Should_successfully_save_exp_record_from_write_function_call() {
        String expectedFunctionResponse = """
                976+0 records in
                976+0 records out
                999424000 bytes (999 MB, 953 MiB) copied, 0.830883 s, 1.2 GB/s""";
        IoSizeTier ioSizeTier = IoSizeTier.TIER_1;
        FileSizeTier fileSizeTier = FileSizeTier.TIER_1;
        String expectedCommand = "if=/dev/zero of=/tmp/file1 bs=500 count=256000";
        CommandRequest commandRequest = new CommandRequest(expectedCommand);
        when(gcfDdFunctionClient.callFunction(commandRequest)).thenReturn(expectedFunctionResponse);
        when(ddExpRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        DdExpRecordEntity savedEntity = gcfDdFunctionService.collectWriteExpRecord(ioSizeTier, fileSizeTier);
        assertEquals(savedEntity.getSystemName(), SystemName.GCF_DD);
        assertEquals(savedEntity.getRawResponse(), expectedFunctionResponse);
        assertEquals(savedEntity.getCommand(), expectedCommand);
        assertEquals(savedEntity.getOperationType(), OperationType.WRITE);
        assertEquals(savedEntity.getFileSizeBytes(), fileSizeTier.getFileSizeBytes());
        assertEquals(savedEntity.getIoSizeBytes(), ioSizeTier.getIoSizeBytes());
        assertEquals(savedEntity.getLatencySeconds(), 0.830883);
        assertEquals(savedEntity.getThroughputKbPerSecond(), 1.2e9);
        assertNotNull(savedEntity.getCollectedAt());
        verify(ddExpRecordRepository).save(any());
    }

    @Test
    public void Should_successfully_post_process_function_response_to_extract_latency_and_throughput() {
        String expectedFunctionResponse = """
                976+0 records in
                976+0 records out
                999424000 bytes (999 MB, 953 MiB) copied, 0.830883 s, 1.2 GB/s""";
        IoSizeTier ioSizeTier = IoSizeTier.TIER_1;
        FileSizeTier fileSizeTier = FileSizeTier.TIER_1;
        String expectedCommand = "if=/dev/zero of=/tmp/file1 bs=500 count=256000";
        CommandRequest commandRequest = new CommandRequest(expectedCommand);
        when(gcfDdFunctionClient.callFunction(commandRequest)).thenReturn(expectedFunctionResponse);
        when(ddExpRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        DdExpRecordEntity savedEntity = gcfDdFunctionService.collectWriteExpRecord(ioSizeTier, fileSizeTier);
        assertEquals(savedEntity.getRawLatency(), "0.830883 s");
        assertEquals(savedEntity.getRawThroughput(), "1.2 GB/s");
    }

    @Test
    public void Should_throw_dd_exception_when_parsing_response_fails() {
        String expectedFunctionResponse = """
                976+0 records in
                976+0 records out
                999424000 bytes (999 MB) copied, 0.830883 s, 1.2 GB/s""";
        IoSizeTier ioSizeTier = IoSizeTier.TIER_1;
        FileSizeTier fileSizeTier = FileSizeTier.TIER_1;
        String expectedCommand = "if=/dev/zero of=/tmp/file1 bs=500 count=256000";
        CommandRequest commandRequest = new CommandRequest(expectedCommand);
        when(gcfDdFunctionClient.callFunction(commandRequest)).thenReturn(expectedFunctionResponse);
        Throwable thrown = assertThrows(DdFunctionException.class, () -> gcfDdFunctionService.collectWriteExpRecord(ioSizeTier, fileSizeTier));
        assertTrue(thrown.getMessage().contains(String.valueOf(ioSizeTier.getIoSizeBytes())));
        assertTrue(thrown.getMessage().contains(String.valueOf(fileSizeTier.getFileSizeBytes())));
        assertTrue(thrown.getMessage().contains(expectedCommand));
        assertTrue(thrown.getMessage().contains(expectedFunctionResponse));
        verify(ddExpRecordRepository, never()).save(any());
    }

    @Test
    public void Should_set_zero_to_throughput_when_unit_not_resolvable() {
        String expectedFunctionResponse = """
                976+0 records in
                976+0 records out
                999424000 bytes (999 MB, 953 MiB) copied, 0.830883 s, 1.2 LB/s""";
        IoSizeTier ioSizeTier = IoSizeTier.TIER_1;
        FileSizeTier fileSizeTier = FileSizeTier.TIER_1;
        String expectedCommand = "if=/dev/zero of=/tmp/file1 bs=500 count=256000";
        CommandRequest commandRequest = new CommandRequest(expectedCommand);
        when(gcfDdFunctionClient.callFunction(commandRequest)).thenReturn(expectedFunctionResponse);
        when(ddExpRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        DdExpRecordEntity savedEntity = gcfDdFunctionService.collectWriteExpRecord(ioSizeTier, fileSizeTier);
        assertEquals(savedEntity.getThroughputKbPerSecond(), 0);
        verify(ddExpRecordRepository).save(any());
    }

    @Test
    public void Should_successfully_save_exp_record_from_read_function_call() {
        String expectedFunctionResponse = """
                953+1 records in
                953+1 records out
                999424000 bytes (999 MB, 953 MiB) copied, 0.296427 s, 3.4 GB/s""";
        IoSizeTier ioSizeTier = IoSizeTier.TIER_1;
        String expectedCommand = "if=/tmp/file1 of=/dev/null bs=500";
        CommandRequest commandRequest = new CommandRequest(expectedCommand);
        when(gcfDdFunctionClient.callFunction(commandRequest)).thenReturn(expectedFunctionResponse);
        when(ddExpRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        DdExpRecordEntity savedEntity = gcfDdFunctionService.collectReadExpRecord(ioSizeTier);
        assertEquals(savedEntity.getSystemName(), SystemName.GCF_DD);
        assertEquals(savedEntity.getRawResponse(), expectedFunctionResponse);
        assertEquals(savedEntity.getCommand(), expectedCommand);
        assertEquals(savedEntity.getOperationType(), OperationType.READ);
        assertNull(savedEntity.getFileSizeBytes());
        assertEquals(savedEntity.getIoSizeBytes(), ioSizeTier.getIoSizeBytes());
        assertEquals(savedEntity.getLatencySeconds(), 0.296427);
        assertEquals(savedEntity.getThroughputKbPerSecond(), 3.4e9);
        assertNotNull(savedEntity.getCollectedAt());
        verify(ddExpRecordRepository).save(any());
    }

    @Test
    public void Should_successfully_set_function_resources() throws GcfNotFoundException {
        String functionName = generator.nextObject(String.class);
        ResourceTier resourceTier = ResourceTier.TIER_4;
        String memory = "1024M";
        String cpu = "0.579";

        when(gcfDdFunctionProps.name()).thenReturn(functionName);

        gcfDdFunctionService.callSetFunctionResources(resourceTier);
        verify(gcfService).setFunctionResources(functionName, memory, cpu);
    }

    @Test
    public void Should_map_to_runtime_exception_if_function_cannot_be_found() throws GcfNotFoundException {
        String functionName = generator.nextObject(String.class);
        ResourceTier resourceTier = ResourceTier.TIER_4;
        String memory = "1024M";
        String cpu = "0.579";

        when(gcfService.setFunctionResources(functionName, memory, cpu)).thenThrow(GcfNotFoundException.class);
        when(gcfDdFunctionProps.name()).thenReturn(functionName);

        assertThrows(DdFunctionException.class, () -> gcfDdFunctionService.callSetFunctionResources(resourceTier));
        verify(gcfService).setFunctionResources(functionName, memory, cpu);
    }
}