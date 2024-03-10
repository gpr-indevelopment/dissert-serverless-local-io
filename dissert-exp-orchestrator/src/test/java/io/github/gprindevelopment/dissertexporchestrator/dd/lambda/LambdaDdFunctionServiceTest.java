package io.github.gprindevelopment.dissertexporchestrator.dd.lambda;

import io.github.gprindevelopment.dissertexporchestrator.aws.LambdaNotFoundException;
import io.github.gprindevelopment.dissertexporchestrator.aws.LambdaResourceTier;
import io.github.gprindevelopment.dissertexporchestrator.aws.LambdaService;
import io.github.gprindevelopment.dissertexporchestrator.aws.LambdaUpdateMaxTriesException;
import io.github.gprindevelopment.dissertexporchestrator.dd.data.DdExperimentEntity;
import io.github.gprindevelopment.dissertexporchestrator.dd.data.DdExperimentService;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.CommandRequest;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.DdFunctionException;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.SystemName;
import io.github.gprindevelopment.dissertexporchestrator.domain.FileSizeTier;
import io.github.gprindevelopment.dissertexporchestrator.domain.IoSizeTier;
import io.github.gprindevelopment.dissertexporchestrator.domain.OperationType;
import io.github.gprindevelopment.dissertexporchestrator.domain.ResourceTier;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LambdaDdFunctionServiceTest {

    @InjectMocks
    private LambdaDdFunctionService lambdaDdFunctionService;
    @Mock
    private LambdaDdFunctionClient lambdaDdFunctionClient;
    @Mock
    private LambdaDdFunctionProps lambdaDdFunctionProps;
    @Mock
    private LambdaService lambdaService;
    @Mock
    private DdExperimentService experimentService;
    private final EasyRandom generator = new EasyRandom();

    @Test
    public void Should_successfully_save_exp_record_from_zero_write_function_call() {
        String expectedFunctionResponse = """
                976+0 records in
                976+0 records out
                999424000 bytes (999 MB) copied,
                1.31127 s,
                762 MB/s
                """;
        IoSizeTier ioSizeTier = IoSizeTier.TIER_1;
        FileSizeTier fileSizeTier = FileSizeTier.TIER_5;
        String expectedCommand = "if=/dev/zero of=/tmp/file1 bs=500 count=256000";
        CommandRequest commandRequest = new CommandRequest(expectedCommand);
        DdExperimentEntity expectedExperiment = new DdExperimentEntity();

        when(experimentService.recordSuccessfulExperiment(
                SystemName.LAMBDA_DD,
                ResourceTier.TIER_1,
                expectedFunctionResponse,
                "1.31127 s",
                "762 MB/s",
                ioSizeTier.getIoSizeBytes(),
                fileSizeTier.getFileSizeBytes(),
                expectedCommand,
                OperationType.WRITE
        )).thenReturn(expectedExperiment);
        when(lambdaDdFunctionClient.callFunction(commandRequest)).thenReturn(expectedFunctionResponse);
        DdExperimentEntity savedEntity = lambdaDdFunctionService.collectZeroWriteExpRecord(ioSizeTier, fileSizeTier);

        assertEquals(expectedExperiment, savedEntity);
    }

    @Test
    public void Should_successfully_save_exp_record_from_urandom_write_function_call() {
        String expectedFunctionResponse = """
                976+0 records in
                976+0 records out
                999424000 bytes (999 MB) copied,
                1.31127 s,
                762 MB/s
                """;
        IoSizeTier ioSizeTier = IoSizeTier.TIER_1;
        FileSizeTier fileSizeTier = FileSizeTier.TIER_5;
        String expectedCommand = "if=/dev/urandom of=/tmp/file1 bs=500 count=256000";
        CommandRequest commandRequest = new CommandRequest(expectedCommand);
        DdExperimentEntity expectedExperiment = new DdExperimentEntity();

        when(experimentService.recordSuccessfulExperiment(
                SystemName.LAMBDA_DD,
                ResourceTier.TIER_1,
                expectedFunctionResponse,
                "1.31127 s",
                "762 MB/s",
                ioSizeTier.getIoSizeBytes(),
                fileSizeTier.getFileSizeBytes(),
                expectedCommand,
                OperationType.WRITE
        )).thenReturn(expectedExperiment);
        when(lambdaDdFunctionClient.callFunction(commandRequest)).thenReturn(expectedFunctionResponse);
        DdExperimentEntity savedEntity = lambdaDdFunctionService.collectURandomWriteExpRecord(ioSizeTier, fileSizeTier);

        assertEquals(expectedExperiment, savedEntity);
    }

    @Test
    public void Should_successfully_save_exp_record_from_read_function_call() {
        String expectedFunctionResponse = """
                953+1 records in
                953+1 records out
                999424000 bytes (999 MB) copied,
                0.130173 s,
                7.7 GB/s""";
        IoSizeTier ioSizeTier = IoSizeTier.TIER_1;
        FileSizeTier fileSizeTier = FileSizeTier.TIER_2;
        String expectedCommand = "if=/tmp/file1 of=/dev/null bs=500";
        CommandRequest commandRequest = new CommandRequest(expectedCommand);
        DdExperimentEntity expectedExperiment = new DdExperimentEntity();

        when(experimentService.recordSuccessfulExperiment(
                SystemName.LAMBDA_DD,
                ResourceTier.TIER_1,
                expectedFunctionResponse,
                "0.130173 s",
                "7.7 GB/s",
                ioSizeTier.getIoSizeBytes(),
                fileSizeTier.getFileSizeBytes(),
                expectedCommand,
                OperationType.READ
        )).thenReturn(expectedExperiment);
        when(lambdaDdFunctionClient.callFunction(commandRequest)).thenReturn(expectedFunctionResponse);
        DdExperimentEntity savedEntity = lambdaDdFunctionService.collectReadExpRecord(ioSizeTier, fileSizeTier);

        assertEquals(expectedExperiment, savedEntity);
    }

    @Test
    public void Should_successfully_set_function_resources() throws LambdaNotFoundException, LambdaUpdateMaxTriesException {
        ResourceTier resourceTier = ResourceTier.TIER_3;
        String functionArn = generator.nextObject(String.class);

        when(lambdaDdFunctionProps.arn()).thenReturn(functionArn);

        lambdaDdFunctionService.callSetFunctionResources(resourceTier);
        verify(lambdaService).setFunctionMemory(functionArn, LambdaResourceTier.TIER_3.getMemoryMbs());
    }

    @Test
    public void Should_map_lambda_not_found_to_dd_function_exception() throws LambdaNotFoundException, LambdaUpdateMaxTriesException {
        ResourceTier resourceTier = ResourceTier.TIER_3;
        String functionArn = generator.nextObject(String.class);

        when(lambdaDdFunctionProps.arn()).thenReturn(functionArn);
        doThrow(LambdaNotFoundException.class).when(lambdaService).setFunctionMemory(functionArn, LambdaResourceTier.TIER_3.getMemoryMbs());

        assertThrows(DdFunctionException.class, () -> lambdaDdFunctionService.callSetFunctionResources(resourceTier));
    }

    @Test
    public void Should_map_update_max_tries_to_dd_function_exception() throws LambdaNotFoundException, LambdaUpdateMaxTriesException {
        ResourceTier resourceTier = ResourceTier.TIER_3;
        String functionArn = generator.nextObject(String.class);

        when(lambdaDdFunctionProps.arn()).thenReturn(functionArn);
        doThrow(LambdaUpdateMaxTriesException.class).when(lambdaService).setFunctionMemory(functionArn, LambdaResourceTier.TIER_3.getMemoryMbs());

        assertThrows(DdFunctionException.class, () -> lambdaDdFunctionService.callSetFunctionResources(resourceTier));
    }
}