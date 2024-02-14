package io.github.gprindevelopment.dissertexporchestrator.dd.lambda;

import io.github.gprindevelopment.dissertexporchestrator.aws.LambdaNotFoundException;
import io.github.gprindevelopment.dissertexporchestrator.aws.LambdaResourceTier;
import io.github.gprindevelopment.dissertexporchestrator.aws.LambdaService;
import io.github.gprindevelopment.dissertexporchestrator.aws.LambdaUpdateMaxTriesException;
import io.github.gprindevelopment.dissertexporchestrator.dd.common.DdFunctionService;
import io.github.gprindevelopment.dissertexporchestrator.dd.data.DdExperimentService;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.CommandRequest;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.DdFunctionException;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.SystemName;
import io.github.gprindevelopment.dissertexporchestrator.domain.ResourceTier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LambdaDdFunctionService extends DdFunctionService {

    private final LambdaDdFunctionClient lambdaDdFunctionClient;
    private final LambdaDdFunctionProps lambdaDdFunctionProps;
    private final LambdaService lambdaService;

    public LambdaDdFunctionService(LambdaDdFunctionClient lambdaDdFunctionClient,
                                   DdExperimentService experimentService,
                                   LambdaDdFunctionProps lambdaDdFunctionProps,
                                   LambdaService lambdaService) {
        super(experimentService);
        this.lambdaDdFunctionClient = lambdaDdFunctionClient;
        this.lambdaDdFunctionProps = lambdaDdFunctionProps;
        this.lambdaService = lambdaService;
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

    @Override
    protected SystemName getSystemName() {
        return SystemName.LAMBDA_DD;
    }

    @Override
    protected void callSetFunctionResources(ResourceTier resourceTier) {
        LambdaResourceTier lambdaResourceTier = LambdaResourceTier.from(resourceTier);
        String lambdaArn = lambdaDdFunctionProps.arn();
        try {
            lambdaService.setFunctionMemory(lambdaArn, lambdaResourceTier.getMemoryMbs());
        } catch (LambdaNotFoundException e) {
            String message = String.format("Unable to find Lambda DD function with ARN: %s when updating to resource tier: %s",
                    lambdaArn,
                    resourceTier);
            throw new DdFunctionException(message, e);
        } catch (LambdaUpdateMaxTriesException e) {
            String message = String.format("Failed due to max tries for Lambda DD function with ARN: %s when updating to resource tier: %s",
                    lambdaArn,
                    resourceTier);
            throw new DdFunctionException(message + lambdaArn, e);
        }
    }
}
