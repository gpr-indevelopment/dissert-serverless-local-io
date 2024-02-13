package io.github.gprindevelopment.dissertexporchestrator;

import io.github.gprindevelopment.dissertexporchestrator.dd.common.DdFunctionService;
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

    @Scheduled(fixedDelayString = "${dissert-exp-orchestrator.experimentation-scheduler.fixedDelayMinutes}", timeUnit = TimeUnit.MINUTES)
    private void run() {
        log.info("Scheduler triggered");
        for (ResourceTier resourceTier : ResourceTier.values()) {
            log.info("Setting resource tier to: {}", resourceTier);
            for (DdFunctionService ddFunctionService : ddFunctionServices) {
                ddFunctionService.setFunctionResources(resourceTier);
            }

            for (IoSizeTier ioSizeTier : IoSizeTier.values()) {
                log.info("Setting IO size bytes to: {}", ioSizeTier.getIoSizeBytes());
                for (FileSizeTier fileSizeTier : FileSizeTier.values()) {
                    log.info("Setting file size bytes to: {}", fileSizeTier.getFileSizeBytes());
                    for (DdFunctionService ddFunctionService : ddFunctionServices) {
                        collectExpRecordIgnoreException(ddFunctionService, ioSizeTier, fileSizeTier);
                    }
                }
            }
        }
    }

    private void collectExpRecordIgnoreException(
            DdFunctionService functionService,
            IoSizeTier ioSizeTier,
            FileSizeTier fileSizeTier
    ) {
        try {
            functionService.collectWriteExpRecord(ioSizeTier, fileSizeTier);
        } catch (Exception ex) {}
    }
}
