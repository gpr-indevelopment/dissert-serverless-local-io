package io.github.gprindevelopment.dissertexporchestrator;

import io.github.gprindevelopment.dissertexporchestrator.dd.common.DdFunctionService;
import io.github.gprindevelopment.dissertexporchestrator.domain.ClockService;
import io.github.gprindevelopment.dissertexporchestrator.domain.FileSizeTier;
import io.github.gprindevelopment.dissertexporchestrator.domain.IoSizeTier;
import io.github.gprindevelopment.dissertexporchestrator.domain.ResourceTier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExperimentationSchedulerTest {

    @InjectMocks
    private ExperimentationScheduler experimentationScheduler;
    @Mock
    private DdFunctionService ddFunctionService;
    @Mock
    private ClockService clockService;
    @Spy
    private List<DdFunctionService> ddFunctionServices = new ArrayList<>();

    @BeforeEach
    public void setup() {
        ddFunctionServices.add(ddFunctionService);
    }

    @Test
    public void Should_not_use_compatible_file_and_resource_tier_combinations() {
        ResourceTier[] resourceTiers = new ResourceTier[]{ResourceTier.TIER_1};
        FileSizeTier[] fileSizeTiers = new FileSizeTier[]{FileSizeTier.TIER_5};
        IoSizeTier[] ioSizeTiers = IoSizeTier.values();

        experimentationScheduler.runExperiments(resourceTiers, fileSizeTiers, ioSizeTiers);
        verify(ddFunctionService).setFunctionResources(ResourceTier.TIER_1);
        verify(ddFunctionService, never()).collectZeroWriteExpRecord(any(), any());
    }

    @Test
    public void Should_use_compatible_file_and_resource_tier_combinations() {
        ResourceTier[] resourceTiers = new ResourceTier[]{ResourceTier.TIER_1};
        FileSizeTier[] fileSizeTiers = new FileSizeTier[]{FileSizeTier.TIER_4};
        IoSizeTier[] ioSizeTiers = IoSizeTier.values();

        experimentationScheduler.runExperiments(resourceTiers, fileSizeTiers, ioSizeTiers);
        verify(ddFunctionService).setFunctionResources(ResourceTier.TIER_1);
        verify(ddFunctionService, times(ioSizeTiers.length)).collectURandomDirectWriteExpRecord(any(), any());
    }

    @Test
    public void Should_set_function_resources_before_collecting_experiments() {
        ResourceTier[] resourceTiers = new ResourceTier[]{ResourceTier.TIER_1, ResourceTier.TIER_2};
        FileSizeTier[] fileSizeTiers = new FileSizeTier[]{FileSizeTier.TIER_4};
        IoSizeTier[] ioSizeTiers = IoSizeTier.values();

        experimentationScheduler.runExperiments(resourceTiers, fileSizeTiers, ioSizeTiers);
        InOrder inOrder = inOrder(ddFunctionService);
        inOrder.verify(ddFunctionService).setFunctionResources(ResourceTier.TIER_1);
        for (int i = 0; i < ioSizeTiers.length; i++) {
            inOrder.verify(ddFunctionService).collectURandomDirectWriteExpRecord(any(), any());
            inOrder.verify(ddFunctionService).collectURandomWriteExpRecord(any(), any());
            inOrder.verify(ddFunctionService).collectReadExpRecord(any(), any());
        }

        inOrder.verify(ddFunctionService).setFunctionResources(ResourceTier.TIER_2);
        for (int i = 0; i < ioSizeTiers.length; i++) {
            inOrder.verify(ddFunctionService).collectURandomDirectWriteExpRecord(any(), any());
            inOrder.verify(ddFunctionService).collectURandomWriteExpRecord(any(), any());
            inOrder.verify(ddFunctionService).collectReadExpRecord(any(), any());
        }
    }

    @Test
    public void Should_run_all_compatible_combinations_of_io_file_sizes() {
        ResourceTier[] resourceTiers = new ResourceTier[]{ResourceTier.TIER_1};
        experimentationScheduler.runExperiments(resourceTiers, FileSizeTier.values(), IoSizeTier.values());
        for (FileSizeTier fileSizeTier : FileSizeTier.values()) {
            if (!fileSizeTier.isCompatibleWith(ResourceTier.TIER_1)) {
                continue;
            }
            for (IoSizeTier ioSizeTier : IoSizeTier.values()) {
                if (!fileSizeTier.isCompatibleWith(ioSizeTier)) {
                    verify(ddFunctionService, never()).collectURandomDirectWriteExpRecord(ioSizeTier, fileSizeTier);
                    verify(ddFunctionService, never()).collectURandomWriteExpRecord(ioSizeTier, fileSizeTier);
                    verify(ddFunctionService, never()).collectReadExpRecord(ioSizeTier, fileSizeTier);
                    continue;
                }
                verify(ddFunctionService).collectURandomDirectWriteExpRecord(ioSizeTier, fileSizeTier);
                verify(ddFunctionService).collectURandomWriteExpRecord(ioSizeTier, fileSizeTier);
                verify(ddFunctionService).collectReadExpRecord(ioSizeTier, fileSizeTier);
            }
        }
    }

    /**
     * It is assumed that non-direct experiments lead to the OS taking a while before actually writing
     * to the file. This can create a race condition or bottleneck if we trigger a direct writing in sequence.
     * To avoid this risk, we do a sync direct write first, and then do non-direct.
     */
    @Test
    public void Should_run_direct_urandom_write_before_non_direct_experiment() {
        ResourceTier[] resourceTiers = new ResourceTier[]{ResourceTier.TIER_1};
        FileSizeTier[] fileSizeTiers = new FileSizeTier[]{FileSizeTier.TIER_1};
        IoSizeTier[] ioSizeTiers = new IoSizeTier[]{IoSizeTier.TIER_1};
        experimentationScheduler.runExperiments(resourceTiers, fileSizeTiers, ioSizeTiers);
        InOrder inOrder = inOrder(ddFunctionService);
        inOrder.verify(ddFunctionService).collectURandomDirectWriteExpRecord(IoSizeTier.TIER_1, FileSizeTier.TIER_1);
        inOrder.verify(ddFunctionService).collectURandomWriteExpRecord(IoSizeTier.TIER_1, FileSizeTier.TIER_1);
    }
}