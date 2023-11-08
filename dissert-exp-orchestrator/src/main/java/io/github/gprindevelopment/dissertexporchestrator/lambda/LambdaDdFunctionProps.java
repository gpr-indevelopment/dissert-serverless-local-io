package io.github.gprindevelopment.dissertexporchestrator.lambda;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dissert-exp-orchestrator.lambda-dd-function")
public record LambdaDdFunctionProps(String url) {
}
