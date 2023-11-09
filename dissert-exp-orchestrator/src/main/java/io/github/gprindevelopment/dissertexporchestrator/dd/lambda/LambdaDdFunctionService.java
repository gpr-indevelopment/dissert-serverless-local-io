package io.github.gprindevelopment.dissertexporchestrator.dd.lambda;

import io.github.gprindevelopment.dissertexporchestrator.dd.domain.CommandRequest;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.DdExpRecordRepository;
import io.github.gprindevelopment.dissertexporchestrator.dd.common.DdFunctionService;
import io.github.gprindevelopment.dissertexporchestrator.domain.ClockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LambdaDdFunctionService extends DdFunctionService {

    private final LambdaDdFunctionClient lambdaDdFunctionClient;

    public LambdaDdFunctionService(LambdaDdFunctionClient lambdaDdFunctionClient,
                                   DdExpRecordRepository ddExpRecordRepository,
                                   ClockService clockService) {
        super(ddExpRecordRepository, clockService);
        this.lambdaDdFunctionClient = lambdaDdFunctionClient;
    }

    @Override
    protected String callFunction(CommandRequest commandRequest) {
        log.info("Collecting lambda-dd exp record for command: {}", commandRequest);
        return lambdaDdFunctionClient.callFunction(commandRequest);
    }

    @Override
    protected String extractRawLatency(String rawResponse) {
        return rawResponse.split(",")[1].trim();
    }

    @Override
    protected String extractRawThroughput(String rawResponse) {
        return rawResponse.split(",")[2].trim();
    }
}
