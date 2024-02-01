package io.github.gprindevelopment.dissertexporchestrator.gcp;

import com.google.cloud.functions.v2.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@RequiredArgsConstructor
public class GcpService {

    private final GcpProps gcpProps;
    private final FunctionServiceClient client;

    public Function getFunction(String functionName) {
        log.info("Retrieving GCF with name: {}", functionName);
        FunctionName fullFunctionName = FunctionName.of(
                        gcpProps.project(),
                        gcpProps.region(),
                        functionName);
        Function result = client.getFunction(fullFunctionName.toString());
        log.info("Successfully retrieved GCF with name: {}", functionName);
        log.debug("Retrieved GCF: {}", result);
        return result;
    }

    public Function setFunctionResources(String functionName, String memory, String cpu) throws ExecutionException, InterruptedException {
        log.info("Updating resources of GCF with name: {}, memory: {} and cpu: {}", functionName, memory, cpu);
        Function existingFunction = getFunction(functionName);
        ServiceConfig existingServiceConfig = existingFunction.getServiceConfig();
        Function toUpdate = Function
                .newBuilder(existingFunction)
                .setServiceConfig(ServiceConfig
                        .newBuilder(existingServiceConfig)
                        .setAvailableCpu(cpu)
                        .setAvailableMemory(memory)
                        .build())
                .build();
        UpdateFunctionRequest updateRequest = UpdateFunctionRequest
                .newBuilder()
                .setFunction(toUpdate)
                .build();
        Function result = client.updateFunctionAsync(updateRequest).get();
        log.info("Successfully updated resources of GCF with name: {}, memory: {} and cpu: {}",
                functionName,
                memory,
                cpu);
        log.debug("Updated GCF: {}", result);
        return result;
    }
}
