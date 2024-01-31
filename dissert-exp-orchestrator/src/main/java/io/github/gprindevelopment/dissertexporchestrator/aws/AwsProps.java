package io.github.gprindevelopment.dissertexporchestrator.aws;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dissert-exp-orchestrator.aws")
public record AwsProps(String region, String profile, int lambdaUpdateWaitSeconds) {
}