package io.github.gprindevelopment.dissertexporchestrator.gcp;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dissert-exp-orchestrator.gcp")
public record GcpProps(String project, String region) {
}
