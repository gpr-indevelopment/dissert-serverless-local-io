package io.github.gprindevelopment.dissertexporchestrator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GcfDdFunctionServiceTest {

    @InjectMocks
    private GcfDdFunctionService gcfDdFunctionService;
    @Mock
    private GcfDdFunctionClient gcfDdFunctionClient;
    @Mock
    private ExpRecordRepository expRecordRepository;

    @Test
    public void Should_successfully_save_exp_record_from_function_call() {
        String expectedFunctionResponse = """
                    1024+0 records in
                    1024+0 records out
                    1073741824 bytes (1.1 GB, 1.0 GiB) copied, 1.09449 s, 981 MB/s""";
        Long ioSize = 1_024_000L;
        Long blockCount = 1_024L;
        String expectedCommand = "if=/dev/zero of=/tmp/file1 bs=1024000 count=1024";
        CommandRequest commandRequest = new CommandRequest(expectedCommand);
        when(gcfDdFunctionClient.callFunction(commandRequest)).thenReturn(expectedFunctionResponse);
        when(expRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        ExpRecordEntity savedEntity = gcfDdFunctionService.collectWriteExpRecord(ioSize, blockCount);
        assertEquals(savedEntity.getSystemName(), "gcf-dd");
        assertEquals(savedEntity.getRawContent(), expectedFunctionResponse);
        assertEquals(savedEntity.getCommand(), expectedCommand);
        assertEquals(savedEntity.getOperationType(), OperationType.WRITE);
        assertNotNull(savedEntity.getCollectedAt());
        verify(expRecordRepository).save(any());
    }

}