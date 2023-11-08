package io.github.gprindevelopment.dissertexporchestrator.gcf;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dissert-exp-orchestrator.gcf-dd-function")
public record GcfDdFunctionProps(String url) {
}