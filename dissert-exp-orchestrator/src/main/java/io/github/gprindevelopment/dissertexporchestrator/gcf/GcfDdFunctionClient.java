package io.github.gprindevelopment.dissertexporchestrator.gcf;

import io.github.gprindevelopment.dissertexporchestrator.common.CommandRequest;
import io.github.gprindevelopment.dissertexporchestrator.common.DdFunctionClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class GcfDdFunctionClient {

    private final DdFunctionClient ddFunctionClient;
    private final GcfDdFunctionProps gcfDdFunctionProps;

    public String callFunction(CommandRequest commandRequest) {
        log.info("Calling gcf-dd-function at url: {} with command: {}", gcfDdFunctionProps.url(), commandRequest);
        String result = ddFunctionClient.callFunction(commandRequest, gcfDdFunctionProps.url());
        log.debug("Received result: {}", result);
        return result;
    }
}
