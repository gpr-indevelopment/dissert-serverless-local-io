package io.github.gprindevelopment.dissertexporchestrator.gcp;

import com.google.cloud.functions.v2.FunctionServiceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;

@Configuration
public class GcpConfig {

    @Bean
    @Lazy
    public FunctionServiceClient functionServiceClient() throws IOException {
        return FunctionServiceClient.create();
    }
}
