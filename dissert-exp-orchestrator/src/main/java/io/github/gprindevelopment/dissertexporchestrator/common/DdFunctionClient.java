package io.github.gprindevelopment.dissertexporchestrator.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
@RequiredArgsConstructor
public class DdFunctionClient {

    private final RestTemplate restTemplate;

    public String callFunction(CommandRequest commandRequest, String url) {
        return restTemplate.postForObject(url, commandRequest, String.class);
    }
}
