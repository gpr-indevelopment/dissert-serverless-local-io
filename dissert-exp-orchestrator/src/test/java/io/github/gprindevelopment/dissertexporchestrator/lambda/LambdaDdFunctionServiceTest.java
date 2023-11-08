package io.github.gprindevelopment.dissertexporchestrator.lambda;

import io.github.gprindevelopment.dissertexporchestrator.common.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

    @Test
    public void Should_successfully_save_exp_record_from_function_call() throws DdFunctionException {
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
        assertEquals(savedEntity.getSystemName(), "gcf-dd");
        assertEquals(savedEntity.getRawResponse(), expectedFunctionResponse);
        assertEquals(savedEntity.getCommand(), expectedCommand);
        assertEquals(savedEntity.getOperationType(), OperationType.WRITE);
        assertEquals(savedEntity.getFileSizeBytes(), fileSize);
        assertEquals(savedEntity.getIoSizeBytes(), ioSizeBytes);
        assertNotNull(savedEntity.getCollectedAt());
        verify(ddExpRecordRepository).save(any());
    }

    @Test
    public void Should_successfully_post_process_function_response_to_extract_latency_and_throughput() throws DdFunctionException {
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
}