package io.github.gprindevelopment.dissertexporchestrator.dd.gcf;

import io.github.gprindevelopment.dissertexporchestrator.dd.data.DdExperimentEntity;
import io.github.gprindevelopment.dissertexporchestrator.dd.data.DdExperimentService;
import io.github.gprindevelopment.dissertexporchestrator.dd.data.SuccessfulExperiment;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.CommandRequest;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.DdExperimentName;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.DdFunctionException;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.SystemName;
import io.github.gprindevelopment.dissertexporchestrator.domain.FileSizeTier;
import io.github.gprindevelopment.dissertexporchestrator.domain.IoSizeTier;
import io.github.gprindevelopment.dissertexporchestrator.domain.OperationType;
import io.github.gprindevelopment.dissertexporchestrator.domain.ResourceTier;
import io.github.gprindevelopment.dissertexporchestrator.gcp.GcfNotFoundException;
import io.github.gprindevelopment.dissertexporchestrator.gcp.GcfService;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private DdExperimentService experimentService;
    private final EasyRandom generator = new EasyRandom();

    @Test
    public void Should_successfully_save_exp_record_from_zero_write_function_call() {
        String expectedFunctionResponse = """
                976+0 records in
                976+0 records out
                999424000 bytes (999 MB, 953 MiB) copied, 0.830883 s, 1.2 GB/s""";
        IoSizeTier ioSizeTier = IoSizeTier.TIER_1;
        FileSizeTier fileSizeTier = FileSizeTier.TIER_5;
        String expectedCommand = "if=/dev/zero of=/tmp/file1 bs=500 count=256000";
        CommandRequest commandRequest = new CommandRequest(expectedCommand);
        DdExperimentEntity expectedExperiment = new DdExperimentEntity();

        when(experimentService.recordSuccessfulExperiment(
                new SuccessfulExperiment(SystemName.GCF_DD,
                        ResourceTier.TIER_1,
                        expectedFunctionResponse,
                        "0.830883 s",
                        "1.2 GB/s",
                        ioSizeTier.getIoSizeBytes(),
                        fileSizeTier.getFileSizeBytes(),
                        expectedCommand,
                        OperationType.WRITE,
                        DdExperimentName.DEV_ZERO_WRITE))).thenReturn(expectedExperiment);
        when(gcfDdFunctionClient.callFunction(commandRequest)).thenReturn(expectedFunctionResponse);

        DdExperimentEntity savedEntity = gcfDdFunctionService.collectZeroWriteExpRecord(ioSizeTier, fileSizeTier);
        assertEquals(expectedExperiment, savedEntity);
    }

    @Test
    public void Should_successfully_save_exp_record_from_urandom_write_function_call() {
        String expectedFunctionResponse = """
                976+0 records in
                976+0 records out
                999424000 bytes (999 MB, 953 MiB) copied, 0.830883 s, 1.2 GB/s""";
        IoSizeTier ioSizeTier = IoSizeTier.TIER_1;
        FileSizeTier fileSizeTier = FileSizeTier.TIER_5;
        String expectedCommand = "if=/dev/urandom of=/tmp/file1 bs=500 count=256000";
        CommandRequest commandRequest = new CommandRequest(expectedCommand);
        DdExperimentEntity expectedExperiment = new DdExperimentEntity();

        when(experimentService.recordSuccessfulExperiment(
                new SuccessfulExperiment(SystemName.GCF_DD,
                        ResourceTier.TIER_1,
                        expectedFunctionResponse,
                        "0.830883 s",
                        "1.2 GB/s",
                        ioSizeTier.getIoSizeBytes(),
                        fileSizeTier.getFileSizeBytes(),
                        expectedCommand,
                        OperationType.WRITE,
                        DdExperimentName.URANDOM_WRITE))).thenReturn(expectedExperiment);
        when(gcfDdFunctionClient.callFunction(commandRequest)).thenReturn(expectedFunctionResponse);

        DdExperimentEntity savedEntity = gcfDdFunctionService.collectURandomWriteExpRecord(ioSizeTier, fileSizeTier);
        assertEquals(expectedExperiment, savedEntity);
    }

    @Test
    public void Should_successfully_save_exp_record_from_read_function_call() {
        String expectedFunctionResponse = """
                953+1 records in
                953+1 records out
                999424000 bytes (999 MB, 953 MiB) copied, 0.296427 s, 3.4 GB/s""";
        IoSizeTier ioSizeTier = IoSizeTier.TIER_1;
        FileSizeTier fileSizeTier = FileSizeTier.TIER_2;
        String expectedCommand = "iflag=direct if=/tmp/file1 of=/dev/null bs=500";
        CommandRequest commandRequest = new CommandRequest(expectedCommand);
        DdExperimentEntity expectedExperiment = new DdExperimentEntity();

        when(experimentService.recordSuccessfulExperiment(
                new SuccessfulExperiment(SystemName.GCF_DD,
                        ResourceTier.TIER_1,
                        expectedFunctionResponse,
                        "0.296427 s",
                        "3.4 GB/s",
                        ioSizeTier.getIoSizeBytes(),
                        fileSizeTier.getFileSizeBytes(),
                        expectedCommand,
                        OperationType.READ,
                        DdExperimentName.DIRECT_READ))).thenReturn(expectedExperiment);
        when(gcfDdFunctionClient.callFunction(commandRequest)).thenReturn(expectedFunctionResponse);

        DdExperimentEntity savedEntity = gcfDdFunctionService.collectReadExpRecord(ioSizeTier, fileSizeTier);
        assertEquals(expectedExperiment, savedEntity);
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