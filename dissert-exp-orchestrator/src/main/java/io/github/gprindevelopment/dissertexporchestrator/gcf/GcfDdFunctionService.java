package io.github.gprindevelopment.dissertexporchestrator.gcf;

import io.github.gprindevelopment.dissertexporchestrator.common.CommandRequest;
import io.github.gprindevelopment.dissertexporchestrator.common.DdExpRecordRepository;
import io.github.gprindevelopment.dissertexporchestrator.common.DdFunctionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GcfDdFunctionService extends DdFunctionService {

    private final GcfDdFunctionClient gcfDdFunctionClient;

    public GcfDdFunctionService(GcfDdFunctionClient gcfDdFunctionClient, DdExpRecordRepository ddExpRecordRepository) {
        super(ddExpRecordRepository);
        this.gcfDdFunctionClient = gcfDdFunctionClient;
    }

    @Override
    protected String callFunction(CommandRequest commandRequest) {
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
