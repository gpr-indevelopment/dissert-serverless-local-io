package io.github.gprindevelopment.dissertexporchestrator.dd.lambda;

import io.github.gprindevelopment.dissertexporchestrator.aws.LambdaNotFoundException;
import io.github.gprindevelopment.dissertexporchestrator.aws.LambdaResourceTier;
import io.github.gprindevelopment.dissertexporchestrator.aws.LambdaService;
import io.github.gprindevelopment.dissertexporchestrator.aws.LambdaUpdateMaxTriesException;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.*;
import io.github.gprindevelopment.dissertexporchestrator.domain.ClockService;
import io.github.gprindevelopment.dissertexporchestrator.domain.OperationType;
import io.github.gprindevelopment.dissertexporchestrator.domain.ResourceTier;
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
class LambdaDdFunctionServiceTest {

    @InjectMocks
    private LambdaDdFunctionService lambdaDdFunctionService;
    @Mock
    private LambdaDdFunctionClient lambdaDdFunctionClient;
    @Mock
    private DdExpRecordRepository ddExpRecordRepository;
    @Mock
    private LambdaDdFunctionProps lambdaDdFunctionProps;
    @Mock
    private LambdaService lambdaService;
    @Spy
    private ClockService clockService;
    private final EasyRandom generator = new EasyRandom();

    @Test
    public void Should_successfully_save_exp_record_from_function_call() {
        String expectedFunctionResponse = """
                976+0 records in
                976+0 records out
                999424000 bytes (999 MB) copied,
                1.31127 s,
                762 MB/s
                """;
        Long ioSizeBytes = 1_024_000L;
        Long fileSize = 1_000_000_000L;
        String expectedCommand = "if=/dev/zero of=/tmp/file1 bs=1024000 count=976";
        CommandRequest commandRequest = new CommandRequest(expectedCommand);
        when(lambdaDdFunctionClient.callFunction(commandRequest)).thenReturn(expectedFunctionResponse);
        when(ddExpRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        DdExpRecordEntity savedEntity = lambdaDdFunctionService.collectWriteExpRecord(ioSizeBytes, fileSize);
        assertEquals(savedEntity.getSystemName(), SystemName.LAMBDA_DD);
        assertEquals(savedEntity.getRawResponse(), expectedFunctionResponse);
        assertEquals(savedEntity.getCommand(), expectedCommand);
        assertEquals(savedEntity.getOperationType(), OperationType.WRITE);
        assertEquals(savedEntity.getFileSizeBytes(), fileSize);
        assertEquals(savedEntity.getIoSizeBytes(), ioSizeBytes);
        assertEquals(savedEntity.getLatencySeconds(), 1.31127);
        assertEquals(savedEntity.getThroughputKbPerSecond(), 7.62e8);
        assertNotNull(savedEntity.getCollectedAt());
        verify(ddExpRecordRepository).save(any());
    }

    @Test
    public void Should_successfully_post_process_function_response_to_extract_latency_and_throughput() {
        String expectedFunctionResponse = """
                976+0 records in
                976+0 records out
                999424000 bytes (999 MB) copied,
                1.31127 s,
                762 MB/s
                """;
        Long ioSizeBytes = 1_024_000L;
        Long fileSize = 1_000_000_000L;
        String expectedCommand = "if=/dev/zero of=/tmp/file1 bs=1024000 count=976";
        CommandRequest commandRequest = new CommandRequest(expectedCommand);
        when(lambdaDdFunctionClient.callFunction(commandRequest)).thenReturn(expectedFunctionResponse);
        when(ddExpRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        DdExpRecordEntity savedEntity = lambdaDdFunctionService.collectWriteExpRecord(ioSizeBytes, fileSize);
        assertEquals(savedEntity.getRawLatency(), "1.31127 s");
        assertEquals(savedEntity.getRawThroughput(), "762 MB/s");
    }

    @Test
    public void Should_throw_dd_exception_when_parsing_response_fails() {
        String expectedFunctionResponse = """
                976+0 records in
                976+0 records out
                999424000 bytes (999 MB) copied - 0.830298 s, 1.2 GB/s""";
        Long ioSizeBytes = 1_024_000L;
        Long fileSize = 1_000_000_000L;
        String expectedCommand = "if=/dev/zero of=/tmp/file1 bs=1024000 count=976";
        CommandRequest commandRequest = new CommandRequest(expectedCommand);
        when(lambdaDdFunctionClient.callFunction(commandRequest)).thenReturn(expectedFunctionResponse);
        Throwable thrown = assertThrows(DdFunctionException.class, () -> lambdaDdFunctionService.collectWriteExpRecord(ioSizeBytes, fileSize));
        assertTrue(thrown.getMessage().contains(ioSizeBytes.toString()));
        assertTrue(thrown.getMessage().contains(fileSize.toString()));
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
        Long ioSizeBytes = 1_024_000L;
        Long fileSize = 1_000_000_000L;
        String expectedCommand = "if=/dev/zero of=/tmp/file1 bs=1024000 count=976";
        CommandRequest commandRequest = new CommandRequest(expectedCommand);
        when(lambdaDdFunctionClient.callFunction(commandRequest)).thenReturn(expectedFunctionResponse);
        when(ddExpRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        DdExpRecordEntity savedEntity = lambdaDdFunctionService.collectWriteExpRecord(ioSizeBytes, fileSize);
        assertEquals(savedEntity.getThroughputKbPerSecond(), 0);
        verify(ddExpRecordRepository).save(any());
    }

    @Test
    public void Should_successfully_save_exp_record_from_read_function_call() {
        String expectedFunctionResponse = """
                953+1 records in
                953+1 records out
                999424000 bytes (999 MB) copied,
                0.130173 s,
                7.7 GB/s""";
        Long ioSizeBytes = 1_024_000L;
        String expectedCommand = "if=/tmp/file1 of=/dev/null bs=1024000";
        CommandRequest commandRequest = new CommandRequest(expectedCommand);
        when(lambdaDdFunctionClient.callFunction(commandRequest)).thenReturn(expectedFunctionResponse);
        when(ddExpRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        DdExpRecordEntity savedEntity = lambdaDdFunctionService.collectReadExpRecord(ioSizeBytes);
        assertEquals(savedEntity.getSystemName(), SystemName.LAMBDA_DD);
        assertEquals(savedEntity.getRawResponse(), expectedFunctionResponse);
        assertEquals(savedEntity.getCommand(), expectedCommand);
        assertEquals(savedEntity.getOperationType(), OperationType.READ);
        assertNull(savedEntity.getFileSizeBytes());
        assertEquals(savedEntity.getIoSizeBytes(), ioSizeBytes);
        assertEquals(savedEntity.getLatencySeconds(), 0.130173);
        assertEquals(savedEntity.getThroughputKbPerSecond(), 7.7e9);
        assertNotNull(savedEntity.getCollectedAt());
        verify(ddExpRecordRepository).save(any());
    }

    @Test
    public void Should_successfully_set_function_resources() throws LambdaNotFoundException, LambdaUpdateMaxTriesException {
        ResourceTier resourceTier = ResourceTier.TIER_3;
        String functionArn = generator.nextObject(String.class);

        when(lambdaDdFunctionProps.arn()).thenReturn(functionArn);

        lambdaDdFunctionService.callSetFunctionResources(resourceTier);
        verify(lambdaService).setFunctionMemory(functionArn, LambdaResourceTier.TIER_3.getMemory());
    }

    @Test
    public void Should_map_lambda_not_found_to_dd_function_exception() throws LambdaNotFoundException, LambdaUpdateMaxTriesException {
        ResourceTier resourceTier = ResourceTier.TIER_3;
        String functionArn = generator.nextObject(String.class);

        when(lambdaDdFunctionProps.arn()).thenReturn(functionArn);
        doThrow(LambdaNotFoundException.class).when(lambdaService).setFunctionMemory(functionArn, LambdaResourceTier.TIER_3.getMemory());

        assertThrows(DdFunctionException.class, () -> lambdaDdFunctionService.callSetFunctionResources(resourceTier));
    }

    @Test
    public void Should_map_update_max_tries_to_dd_function_exception() throws LambdaNotFoundException, LambdaUpdateMaxTriesException {
        ResourceTier resourceTier = ResourceTier.TIER_3;
        String functionArn = generator.nextObject(String.class);

        when(lambdaDdFunctionProps.arn()).thenReturn(functionArn);
        doThrow(LambdaUpdateMaxTriesException.class).when(lambdaService).setFunctionMemory(functionArn, LambdaResourceTier.TIER_3.getMemory());

        assertThrows(DdFunctionException.class, () -> lambdaDdFunctionService.callSetFunctionResources(resourceTier));
    }
}