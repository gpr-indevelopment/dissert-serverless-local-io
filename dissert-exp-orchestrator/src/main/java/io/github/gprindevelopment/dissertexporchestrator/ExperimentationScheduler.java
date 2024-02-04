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

@Component
@RequiredArgsConstructor
@Slf4j
public class ExperimentationScheduler {

    private final List<DdFunctionService> ddFunctionServices;

    @Scheduled(cron = "${dissert-exp-orchestrator.experimentation-scheduler.cron}")
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
                        ddFunctionService.collectWriteExpRecord(ioSizeTier, fileSizeTier);
                    }
                }
            }
        }
    }
}
