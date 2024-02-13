package io.github.gprindevelopment.dissertexporchestrator.dd.common;

import io.github.gprindevelopment.dissertexporchestrator.dd.data.DdExperimentService;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.CommandRequest;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.DdFunctionException;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.SystemName;
import io.github.gprindevelopment.dissertexporchestrator.domain.FileSizeTier;
import io.github.gprindevelopment.dissertexporchestrator.domain.IoSizeTier;
import io.github.gprindevelopment.dissertexporchestrator.domain.ResourceTier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DdFunctionServiceTest {

    @InjectMocks
    @Spy
    private DdFunctionStubService ddFunctionStubService;
    @Mock
    private DdExperimentService experimentService;

    @Test
    public void Should_set_current_resource_tier_as_one_when_null_during_collect_write_record() {
        ddFunctionStubService.currentResourceTier = null;
        IoSizeTier ioSizeTier = IoSizeTier.TIER_1;
        FileSizeTier fileSizeTier = FileSizeTier.TIER_1;

        ddFunctionStubService.collectWriteExpRecord(ioSizeTier, fileSizeTier);
        verify(experimentService).recordSuccessfulExperiment(any(), any(), any(), any(), any(), any(), any(), any(), any());
        assertEquals(ResourceTier.TIER_1, ddFunctionStubService.currentResourceTier);
    }

    @Test
    public void Should_set_current_resource_tier_as_one_when_null_during_collect_read_record() {
        ddFunctionStubService.currentResourceTier = null;
        IoSizeTier ioSizeTier = IoSizeTier.TIER_1;
        ddFunctionStubService.collectReadExpRecord(ioSizeTier);
        verify(experimentService).recordSuccessfulExperiment(any(), any(), any(), any(), any(), any(), any(), any(), any());
        assertEquals(ResourceTier.TIER_1, ddFunctionStubService.currentResourceTier);
    }

    @Test
    public void Should_set_current_resource_tier_when_setting_resource_tier() {
        ddFunctionStubService.currentResourceTier = null;
        ResourceTier resourceTier = ResourceTier.TIER_1;

        assertNull(ddFunctionStubService.currentResourceTier);
        ddFunctionStubService.setFunctionResources(resourceTier);
        assertEquals(resourceTier, ddFunctionStubService.currentResourceTier);
    }

    @Test
    public void Should_throw_exception_when_function_call_fails_on_write() {
        IoSizeTier ioSizeTier = IoSizeTier.TIER_1;
        FileSizeTier fileSizeTier = FileSizeTier.TIER_1;

        doThrow(RuntimeException.class).when(ddFunctionStubService).callFunction(any());

        assertThrows(DdFunctionException.class, () -> ddFunctionStubService.collectWriteExpRecord(ioSizeTier, fileSizeTier));
    }

    @Test
    public void Should_throw_exception_when_function_call_fails_on_read() {
        IoSizeTier ioSizeTier = IoSizeTier.TIER_1;

        doThrow(RuntimeException.class).when(ddFunctionStubService).callFunction(any());

        assertThrows(DdFunctionException.class, () -> ddFunctionStubService.collectReadExpRecord(ioSizeTier));
    }

    @Test
    public void Should_persist_error_record_when_read_operation_fails() {
        ddFunctionStubService.currentResourceTier = ResourceTier.TIER_2;
        IoSizeTier ioSizeTier = IoSizeTier.TIER_1;

        doThrow(RuntimeException.class).when(ddFunctionStubService).callFunction(any());

        assertThrows(DdFunctionException.class, () -> ddFunctionStubService.collectReadExpRecord(ioSizeTier));

        verify(experimentService).recordFailedExperiment(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void Should_persist_error_record_when_write_operation_fails() {
        ddFunctionStubService.currentResourceTier = ResourceTier.TIER_2;
        IoSizeTier ioSizeTier = IoSizeTier.TIER_1;
        FileSizeTier fileSizeTier = FileSizeTier.TIER_2;

        doThrow(RuntimeException.class).when(ddFunctionStubService).callFunction(any());

        assertThrows(DdFunctionException.class, () -> ddFunctionStubService.collectWriteExpRecord(ioSizeTier, fileSizeTier));
        verify(experimentService).recordFailedExperiment(any(), any(), any(), any(), any(), any(), any());
    }

    private static class DdFunctionStubService extends DdFunctionService {

        public DdFunctionStubService(
                DdExperimentService experimentService) {
            super(experimentService);
        }

        @Override
        protected String callFunction(CommandRequest commandRequest) {
            return """
                976+0 records in
                976+0 records out
                999424000 bytes (999 MB, 953 MiB) copied, 0.830883 s, 1.2 GB/s""";
        }

        @Override
        protected String extractRawLatency(String rawResponse) {
            return rawResponse.split(",")[2].trim();
        }

        @Override
        protected String extractRawThroughput(String rawResponse) {
            return rawResponse.split(",")[3].trim();
        }

        @Override
        protected SystemName getSystemName() {
            return SystemName.GCF_DD;
        }

        @Override
        public void callSetFunctionResources(ResourceTier resourceTier) {}
    }

}