package io.github.gprindevelopment.dissertexporchestrator.gcp;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.api.gax.rpc.NotFoundException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GcfServiceTest {

    @Spy
    @InjectMocks
    private GcfService gcfService;
    @Mock
    private GcpProps gcpProps;
    @Mock
    private FunctionServiceClient client;
    private final EasyRandom generator = new EasyRandom();

    @Test
    @SuppressWarnings("unchecked")
    public void Should_successfully_set_function_resources() throws ExecutionException, InterruptedException, GcfNotFoundException {
        String functionName = generator.nextObject(String.class);
        GcfResourceTier tier = GcfResourceTier.TIER_1;
        Function expectedFunction = Function.newBuilder().build();
        OperationFuture<Function, OperationMetadata> operationFutureMock = mock(OperationFuture.class);
        ArgumentCaptor<UpdateFunctionRequest> updateFunctionRequestCaptor = ArgumentCaptor.forClass(UpdateFunctionRequest.class);

        doReturn(expectedFunction).when(gcfService).getFunction(functionName);
        when(client.updateFunctionAsync(updateFunctionRequestCaptor.capture())).thenReturn(operationFutureMock);
        when(operationFutureMock.get()).thenAnswer(i -> updateFunctionRequestCaptor.getValue().getFunction());

        Function actualFunction = gcfService.setFunctionResources(functionName, tier);
        assertEquals(tier.getMemory(), actualFunction.getServiceConfig().getAvailableMemory());
        assertEquals(tier.getCpu(), actualFunction.getServiceConfig().getAvailableCpu());
    }

    @Test
    public void Should_successfully_get_function_from_name() throws GcfNotFoundException {
        String functionName = generator.nextObject(String.class);
        String project = generator.nextObject(String.class);
        String region = generator.nextObject(String.class);
        FunctionName expectedFunctionName = FunctionName.of(
                project,
                region,
                functionName);
        Function expectedFunction = Function.newBuilder().build();

        when(gcpProps.project()).thenReturn(project);
        when(gcpProps.region()).thenReturn(region);
        when(client.getFunction(expectedFunctionName)).thenReturn(expectedFunction);

        Function function = gcfService.getFunction(functionName);
        assertEquals(expectedFunction, function);
    }

    @Test
    public void Should_throw_gcf_not_found_when_function_not_found() {
        String functioName = generator.nextObject(String.class);

        when(gcpProps.project()).thenReturn(generator.nextObject(String.class));
        when(gcpProps.region()).thenReturn(generator.nextObject(String.class));
        when(client.getFunction(any(FunctionName.class))).thenThrow(NotFoundException.class);

        assertThrows(GcfNotFoundException.class, () -> gcfService.getFunction(functioName));
    }

    @Test
    public void Should_map_to_gcf_exception_when_unmapped_error_occurs_in_get_function() {
        String functioName = generator.nextObject(String.class);

        when(gcpProps.project()).thenReturn(generator.nextObject(String.class));
        when(gcpProps.region()).thenReturn(generator.nextObject(String.class));
        when(client.getFunction(any(FunctionName.class))).thenThrow(RuntimeException.class);

        assertThrows(GcfOperationException.class, () -> gcfService.getFunction(functioName));
    }

    @Test
    public void Should_map_to_gcf_exception_when_unmapped_error_occurs_in_set_function_resources() throws GcfNotFoundException {
        String functionName = generator.nextObject(String.class);
        GcfResourceTier tier = GcfResourceTier.TIER_1;
        Function expectedFunction = Function.newBuilder().build();

        doReturn(expectedFunction).when(gcfService).getFunction(functionName);
        when(client.updateFunctionAsync(any())).thenThrow(RuntimeException.class);

        assertThrows(GcfOperationException.class, () -> gcfService.setFunctionResources(functionName, tier));
    }

    @Test
    public void Should_throw_gcf_not_found_when_not_found_while_setting_resources() throws GcfNotFoundException {
        String functionName = generator.nextObject(String.class);
        GcfResourceTier tier = GcfResourceTier.TIER_1;

        doThrow(GcfNotFoundException.class).when(gcfService).getFunction(functionName);

        assertThrows(GcfNotFoundException.class, () -> gcfService.setFunctionResources(functionName, tier));
    }
}
