package io.github.gprindevelopment.dissertexporchestrator;

import io.github.gprindevelopment.dissertexporchestrator.dd.common.DdFunctionService;
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
        verify(ddFunctionService, never()).collectWriteExpRecord(any(), any());
    }

    @Test
    public void Should_use_compatible_file_and_resource_tier_combinations() {
        ResourceTier[] resourceTiers = new ResourceTier[]{ResourceTier.TIER_1};
        FileSizeTier[] fileSizeTiers = new FileSizeTier[]{FileSizeTier.TIER_4};
        IoSizeTier[] ioSizeTiers = IoSizeTier.values();

        experimentationScheduler.runExperiments(resourceTiers, fileSizeTiers, ioSizeTiers);
        verify(ddFunctionService).setFunctionResources(ResourceTier.TIER_1);
        verify(ddFunctionService, times(ioSizeTiers.length)).collectWriteExpRecord(any(), any());
    }

    @Test
    public void Should_set_function_resources_before_collecting_experiments() {
        ResourceTier[] resourceTiers = new ResourceTier[]{ResourceTier.TIER_1, ResourceTier.TIER_2};
        FileSizeTier[] fileSizeTiers = new FileSizeTier[]{FileSizeTier.TIER_4};
        IoSizeTier[] ioSizeTiers = IoSizeTier.values();

        experimentationScheduler.runExperiments(resourceTiers, fileSizeTiers, ioSizeTiers);
        InOrder inOrder = inOrder(ddFunctionService);
        inOrder.verify(ddFunctionService).setFunctionResources(ResourceTier.TIER_1);
        inOrder.verify(ddFunctionService, times(ioSizeTiers.length)).collectWriteExpRecord(any(), any());
        inOrder.verify(ddFunctionService).setFunctionResources(ResourceTier.TIER_2);
        inOrder.verify(ddFunctionService, times(ioSizeTiers.length)).collectWriteExpRecord(any(), any());
    }
}