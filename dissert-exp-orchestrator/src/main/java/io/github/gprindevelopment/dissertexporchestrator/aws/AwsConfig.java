package io.github.gprindevelopment.dissertexporchestrator.aws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;

@Configuration
public class AwsConfig {

    @Bean
    public LambdaClient lambdaClient(AwsProps awsProps) {
        return LambdaClient.builder()
                .region(Region.of(awsProps.region()))
                .build();
    }
}
