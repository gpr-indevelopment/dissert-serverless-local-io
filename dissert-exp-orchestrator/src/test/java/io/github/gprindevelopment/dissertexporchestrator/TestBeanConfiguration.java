package io.github.gprindevelopment.dissertexporchestrator;

import com.google.cloud.functions.v2.FunctionServiceClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.services.lambda.LambdaClient;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestBeanConfiguration {

    @Bean
    @Primary
    public LambdaClient testLambdaClient() {
        return mock(LambdaClient.class);
    }

    @Bean
    @Primary
    public FunctionServiceClient testFunctionServiceClient() {
        return mock(FunctionServiceClient.class);
    }
}
