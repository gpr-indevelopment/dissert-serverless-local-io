package io.github.gprindevelopment.dissertexporchestrator.gcp;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.functions.v2.*;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GcpServiceTest {

    @Spy
    @InjectMocks
    private GcpService gcpService;
    @Mock
    private GcpProps gcpProps;
    @Mock
    private FunctionServiceClient client;
    private final EasyRandom generator = new EasyRandom();

    @Test
    @SuppressWarnings("unchecked")
    public void Should_successfully_set_function_resources() throws ExecutionException, InterruptedException {
        String functionName = generator.nextObject(String.class);
        String memory = "2G";
        String cpu = "1";
        Function expectedFunction = Function.newBuilder().build();
        OperationFuture<Function, OperationMetadata> operationFutureMock = mock(OperationFuture.class);
        ArgumentCaptor<UpdateFunctionRequest> updateFunctionRequestCaptor = ArgumentCaptor.forClass(UpdateFunctionRequest.class);

        doReturn(expectedFunction).when(gcpService).getFunction(functionName);
        when(client.updateFunctionAsync(updateFunctionRequestCaptor.capture())).thenReturn(operationFutureMock);
        when(operationFutureMock.get()).thenAnswer(i -> updateFunctionRequestCaptor.getValue().getFunction());

        Function actualFunction = gcpService.setFunctionResources(functionName, memory, cpu);
        assertEquals(memory, actualFunction.getServiceConfig().getAvailableMemory());
        assertEquals(cpu, actualFunction.getServiceConfig().getAvailableCpu());
    }

    @Test
    public void Should_successfully_get_function_from_name() {
        String functionName = generator.nextObject(String.class);
        String project = generator.nextObject(String.class);
        String region = generator.nextObject(String.class);
        String expectedFunctionName = FunctionName.of(
                project,
                region,
                functionName)
                .toString();
        Function expectedFunction = Function.newBuilder().build();

        when(gcpProps.project()).thenReturn(project);
        when(gcpProps.region()).thenReturn(region);
        when(client.getFunction(expectedFunctionName)).thenReturn(expectedFunction);

        Function function = gcpService.getFunction(functionName);
        assertEquals(expectedFunction, function);
    }

    /*WIP
    What if we cannot find function on get?
    What if update fails due to memory or cpu constraints?
    How to come up with a range of allowed values for resource updates?
    Other error handling...
    */
}
