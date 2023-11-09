package io.github.gprindevelopment.dissertexporchestrator.dd.gcf;

import io.github.gprindevelopment.dissertexporchestrator.dd.domain.CommandRequest;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.DdExpRecordRepository;
import io.github.gprindevelopment.dissertexporchestrator.dd.common.DdFunctionService;
import io.github.gprindevelopment.dissertexporchestrator.domain.ClockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GcfDdFunctionService extends DdFunctionService {

    private final GcfDdFunctionClient gcfDdFunctionClient;

    public GcfDdFunctionService(GcfDdFunctionClient gcfDdFunctionClient,
                                DdExpRecordRepository ddExpRecordRepository,
                                ClockService clockService) {
        super(ddExpRecordRepository, clockService);
        this.gcfDdFunctionClient = gcfDdFunctionClient;
    }

    @Override
    protected String callFunction(CommandRequest commandRequest) {
        log.info("Collecting gcf-dd exp record for command: {}", commandRequest);
        return gcfDdFunctionClient.callFunction(commandRequest);
    }

    @Override
    protected String extractRawLatency(String rawResponse) {
        return rawResponse.split(",")[2].trim();
    }

    @Override
    protected String extractRawThroughput(String rawResponse) {
        return rawResponse.split(",")[3].trim();
    }
}
