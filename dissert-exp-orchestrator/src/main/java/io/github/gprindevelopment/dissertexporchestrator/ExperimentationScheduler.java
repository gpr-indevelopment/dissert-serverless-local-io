package io.github.gprindevelopment.dissertexporchestrator;

import io.github.gprindevelopment.dissertexporchestrator.dd.domain.DdFunctionException;
import io.github.gprindevelopment.dissertexporchestrator.dd.gcf.GcfDdFunctionService;
import io.github.gprindevelopment.dissertexporchestrator.dd.lambda.LambdaDdFunctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExperimentationScheduler {

    private final GcfDdFunctionService gcfDdFunctionService;
    private final LambdaDdFunctionService lambdaDdFunctionService;

    @Scheduled(cron = "${dissert-exp-orchestrator.experimentation-scheduler.cron}")
    private void run() throws DdFunctionException {
        log.info("Scheduler triggered");
        Long ioSizeBytes = 1_024_000L;
        Long fileSize = 1_000_000_000L;
        gcfDdFunctionService.collectWriteExpRecord(ioSizeBytes, fileSize);
        gcfDdFunctionService.collectReadExpRecord(ioSizeBytes);
        lambdaDdFunctionService.collectWriteExpRecord(ioSizeBytes, fileSize);
        lambdaDdFunctionService.collectReadExpRecord(ioSizeBytes);
    }
}
