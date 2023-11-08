package io.github.gprindevelopment.dissertexporchestrator.dd.lambda;

import io.github.gprindevelopment.dissertexporchestrator.dd.domain.CommandRequest;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.DdExpRecordRepository;
import io.github.gprindevelopment.dissertexporchestrator.dd.common.DdFunctionService;
import org.springframework.stereotype.Service;

@Service
public class LambdaDdFunctionService extends DdFunctionService {

    private final LambdaDdFunctionClient lambdaDdFunctionClient;

    public LambdaDdFunctionService(LambdaDdFunctionClient lambdaDdFunctionClient, DdExpRecordRepository ddExpRecordRepository) {
        super(ddExpRecordRepository);
        this.lambdaDdFunctionClient = lambdaDdFunctionClient;
    }

    @Override
    protected String callFunction(CommandRequest commandRequest) {
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
