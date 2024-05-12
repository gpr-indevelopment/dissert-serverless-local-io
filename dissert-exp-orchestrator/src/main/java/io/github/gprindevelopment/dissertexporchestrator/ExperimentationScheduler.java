package io.github.gprindevelopment.dissertexporchestrator;

import io.github.gprindevelopment.dissertexporchestrator.dd.common.DdFunctionService;
import io.github.gprindevelopment.dissertexporchestrator.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExperimentationScheduler {

    private final List<DdFunctionService> ddFunctionServices;
    private final ClockService clockService;

    @Scheduled(fixedDelayString = "${dissert-exp-orchestrator.experimentation-scheduler.fixedDelayMinutes}", timeUnit = TimeUnit.MINUTES)
    public void runAllExperiments() {
        log.info("Scheduler triggered");
        //runFullExperiment(ResourceTier.values(), FileSizeTier.values(), IoSizeTier.values());
        runCachedReadExperiment();
    }

    public void runCachedReadExperiment() {
        Consumer<ExperimentSettings> experiment = (experimentSettings) -> {
            experimentSettings.functionService().collectURandomWriteExpRecord(experimentSettings.ioSizeTier(),  experimentSettings.fileSizeTier());
            experimentSettings.functionService().collectCachedReadExpRecord(experimentSettings.ioSizeTier(),  experimentSettings.fileSizeTier());
        };
        ResourceTier[] resourceTiers = new ResourceTier[]{ResourceTier.TIER_1, ResourceTier.TIER_5};
        FileSizeTier[] fileSizeTiers = new FileSizeTier[]{FileSizeTier.TIER_1, FileSizeTier.TIER_8};
        IoSizeTier[] ioSizeTiers = new IoSizeTier[]{IoSizeTier.TIER_1, IoSizeTier.TIER_9};
        runExperiments(resourceTiers, fileSizeTiers, ioSizeTiers, experiment);
    }

    public void runFullExperiment(ResourceTier[] resourceTiers, FileSizeTier[] fileSizeTiers, IoSizeTier[] ioSizeTiers) {
        Consumer<ExperimentSettings> experiment = (experimentSettings) -> {
            experimentSettings.functionService().collectURandomDirectWriteExpRecord(experimentSettings.ioSizeTier(), experimentSettings.fileSizeTier());
            experimentSettings.functionService().collectURandomWriteExpRecord(experimentSettings.ioSizeTier(),  experimentSettings.fileSizeTier());
            clockService.wait(TimeUnit.SECONDS, 5);
            experimentSettings.functionService().collectDirectReadExpRecord(experimentSettings.ioSizeTier(),  experimentSettings.fileSizeTier());
        };
        runExperiments(resourceTiers, fileSizeTiers, ioSizeTiers, experiment);
    }

    public void runExperiments(ResourceTier[] resourceTiers, FileSizeTier[] fileSizeTiers, IoSizeTier[] ioSizeTiers, Consumer<ExperimentSettings> experiment) {
        for (ResourceTier resourceTier : resourceTiers) {
            setupResourceTier(resourceTier);
            for (FileSizeTier fileSizeTier : fileSizeTiers) {
                if (!fileSizeTier.isCompatibleWith(resourceTier)) {
                    log.info("Resource tier is not compatible with file size. Will skip. ResourceTier: {}, FileSizeTier: {}",
                            resourceTier,
                            fileSizeTier);
                    continue;
                }
                log.info("Setting file size to: {} bytes", fileSizeTier.getFileSizeBytes());
                for (IoSizeTier ioSizeTier : ioSizeTiers) {
                    if (!fileSizeTier.isCompatibleWith(ioSizeTier)) {
                        log.debug("FileSizeTier {} is not compatible with IoSizeTier: {}. Will skip.", fileSizeTier, ioSizeTier);
                        continue;
                    }
                    log.info("Setting IO size to: {} bytes", ioSizeTier.getIoSizeBytes());
                    for (DdFunctionService ddFunctionService : ddFunctionServices) {
                        experiment.accept(new ExperimentSettings(ddFunctionService, ioSizeTier, fileSizeTier));
                    }
                }
            }
        }
    }

    private void setupResourceTier(ResourceTier resourceTier) {
        log.info("Setting resource tier to: {}", resourceTier);
        for (DdFunctionService ddFunctionService : ddFunctionServices) {
            ddFunctionService.setFunctionResources(resourceTier);
        }
    }
}
