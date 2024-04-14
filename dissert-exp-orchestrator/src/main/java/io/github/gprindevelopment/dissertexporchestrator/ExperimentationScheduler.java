package io.github.gprindevelopment.dissertexporchestrator;

import io.github.gprindevelopment.dissertexporchestrator.dd.common.DdFunctionService;
import io.github.gprindevelopment.dissertexporchestrator.domain.ClockService;
import io.github.gprindevelopment.dissertexporchestrator.domain.FileSizeTier;
import io.github.gprindevelopment.dissertexporchestrator.domain.IoSizeTier;
import io.github.gprindevelopment.dissertexporchestrator.domain.ResourceTier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExperimentationScheduler {

    private final List<DdFunctionService> ddFunctionServices;
    private final ClockService clockService;

    @Scheduled(fixedDelayString = "${dissert-exp-orchestrator.experimentation-scheduler.fixedDelayMinutes}", timeUnit = TimeUnit.MINUTES)
    public void runAllExperiments() {
        log.info("Scheduler triggered");
        runExperiments(ResourceTier.values(), FileSizeTier.values(), IoSizeTier.values());
    }

    public void runExperiments(ResourceTier[] resourceTiers, FileSizeTier[] fileSizeTiers, IoSizeTier[] ioSizeTiers) {
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
                        ddFunctionService.collectURandomDirectWriteExpRecord(ioSizeTier, fileSizeTier);
                        clockService.wait(TimeUnit.SECONDS, 5);
                        ddFunctionService.collectReadExpRecord(ioSizeTier, fileSizeTier);
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
