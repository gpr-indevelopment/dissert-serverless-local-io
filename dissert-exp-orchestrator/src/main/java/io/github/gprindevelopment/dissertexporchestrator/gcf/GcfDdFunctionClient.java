package io.github.gprindevelopment.dissertexporchestrator.gcf;

import io.github.gprindevelopment.dissertexporchestrator.common.CommandRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class GcfDdFunctionClient {

    private final RestTemplate restTemplate;
    private final GcfDdFunctionProps gcfDdFunctionProps;

    public String callFunction(CommandRequest commandRequest) {
        log.info("Calling gcf-dd-function at url: {} with command: {}", gcfDdFunctionProps.url(), commandRequest);
        String result = restTemplate.postForObject(gcfDdFunctionProps.url(), commandRequest, String.class);
        log.debug("Received result: {}", result);
        return result;
    }
}
