package io.github.gprindevelopment.dissertexporchestrator.gcp;

import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.functions.v2.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GcfService {

    private final GcpProps gcpProps;
    private final FunctionServiceClient client;

    public Function getFunction(String functionName) throws GcfNotFoundException {
        log.info("Retrieving GCF with name: {}", functionName);
        FunctionName fullFunctionName = FunctionName.of(
                        gcpProps.project(),
                        gcpProps.region(),
                        functionName);
        try {
            Function result = client.getFunction(fullFunctionName);
            log.info("Successfully retrieved GCF with name: {}", functionName);
            log.debug("Retrieved GCF: {}", result);
            return result;
        } catch (NotFoundException notFoundEx) {
            String message = String.format("Gcf not found for functionName: %s", functionName);
            throw new GcfNotFoundException(message, notFoundEx);
        } catch (Exception ex) {
            String message = String.format("Unmapped exception when getting function with name: %s", functionName);
            throw new GcfOperationException(message, ex);
        }
    }

    public Function setFunctionResources(String functionName, String memory, String cpu) throws GcfNotFoundException {
        log.info("Updating resources of GCF with name: {}, memory: {} and cpu: {}",
                functionName,
                memory,
                cpu);
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
        return updateFunctionSync(updateRequest);
    }

    private Function updateFunctionSync(UpdateFunctionRequest updateRequest) {
        String functionName = updateRequest.getFunction().getName();
        String memory = updateRequest.getFunction().getServiceConfig().getAvailableMemory();
        String cpu = updateRequest.getFunction().getServiceConfig().getAvailableCpu();
        try {
            Function result = client.updateFunctionAsync(updateRequest).get();
            log.info("Successfully updated resources of GCF with name: {}, memory: {} and cpu: {}",
                    functionName,
                    memory,
                    cpu);
            log.debug("Updated GCF: {}", result);
            return result;
        } catch (Exception ex) {
            String message = String.format("Unmapped exception when setting resources for function with name: %s, memory: %s and cpu: %s",
                    functionName,
                    memory,
                    cpu);
            throw new GcfOperationException(message, ex);
        }
    }
}
