package io.github.gprindevelopment.dissertexporchestrator.dd.lambda;

import io.github.gprindevelopment.dissertexporchestrator.dd.domain.CommandRequest;
import io.github.gprindevelopment.dissertexporchestrator.dd.common.DdFunctionClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class LambdaDdFunctionClient {

    private final DdFunctionClient ddFunctionClient;
    private final LambdaDdFunctionProps lambdaDdFunctionProps;

    public String callFunction(CommandRequest commandRequest) {
        log.info("Calling lambda-dd-function at url: {} with command: {}", lambdaDdFunctionProps.url(), commandRequest);
        String result = ddFunctionClient.callFunction(commandRequest, lambdaDdFunctionProps.url());
        log.debug("Received result: {}", result);
        return result;
    }
}
