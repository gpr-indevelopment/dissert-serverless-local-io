package io.github.gprindevelopment.dissertexporchestrator.aws;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LambdaServiceTest {

    @InjectMocks
    private LambdaService lambdaService;
    @Mock
    private LambdaClient lambdaClient;
    @Mock
    private AwsProps awsProps;
    private final EasyRandom generator = new EasyRandom();

    @Test
    public void Should_successfully_set_function_memory() throws LambdaNotFoundException, LambdaUpdateMaxTriesException {
        String functionArn = generator.nextObject(String.class);
        int memorySizeMb = 128;
        UpdateFunctionConfigurationRequest expectedRequest = updateRequestWith(functionArn, memorySizeMb);
        UpdateFunctionConfigurationResponse response = updateResponseWith(LastUpdateStatus.SUCCESSFUL);

        when(lambdaClient.updateFunctionConfiguration(expectedRequest)).thenReturn(response);

        lambdaService.setFunctionMemory(functionArn, memorySizeMb);
        verify(lambdaClient).updateFunctionConfiguration(expectedRequest);
        verify(lambdaClient, never()).getFunctionConfiguration(any(GetFunctionConfigurationRequest.class));
    }

    @Test
    public void Should_fail_when_function_in_failed_state() {
        String functionArn = generator.nextObject(String.class);
        int memorySizeMb = 128;
        UpdateFunctionConfigurationRequest expectedRequest = updateRequestWith(functionArn, memorySizeMb);
        UpdateFunctionConfigurationResponse response = updateResponseWith(LastUpdateStatus.FAILED);

        when(lambdaClient.updateFunctionConfiguration(expectedRequest)).thenReturn(response);

        Throwable thrown = assertThrows(AwsOperationException.class, () -> lambdaService.setFunctionMemory(functionArn, memorySizeMb));
        assertTrue(thrown.getMessage().contains(String.valueOf(memorySizeMb)));
        assertTrue(thrown.getMessage().contains(String.valueOf(functionArn)));
        verify(lambdaClient).updateFunctionConfiguration(expectedRequest);
    }

    @Test
    public void Should_try_get_config_for_success_when_function_update_in_progress() throws LambdaNotFoundException, LambdaUpdateMaxTriesException {
        String functionArn = generator.nextObject(String.class);
        int memorySizeMb = 128;
        UpdateFunctionConfigurationRequest expectedUpdateRequest = updateRequestWith(functionArn, memorySizeMb);
        UpdateFunctionConfigurationResponse firstStatusResponse = updateResponseWith(LastUpdateStatus.IN_PROGRESS);
        GetFunctionConfigurationRequest getConfigRequest = getConfigRequestWith(functionArn);
        GetFunctionConfigurationResponse secondStatusResponse = getConfigResponseWith(LastUpdateStatus.SUCCESSFUL);

        when(lambdaClient.updateFunctionConfiguration(expectedUpdateRequest)).thenReturn(firstStatusResponse);
        when(lambdaClient.getFunctionConfiguration(getConfigRequest)).thenReturn(secondStatusResponse);

        lambdaService.setFunctionMemory(functionArn, memorySizeMb);
        verify(lambdaClient).updateFunctionConfiguration(expectedUpdateRequest);
        verify(lambdaClient).getFunctionConfiguration(getConfigRequest);
    }

    @Test
    public void Should_succeed_if_retried_get_config_for_success_when_function_update_in_progress() throws LambdaNotFoundException, LambdaUpdateMaxTriesException {
        String functionArn = generator.nextObject(String.class);
        int memorySizeMb = 128;
        UpdateFunctionConfigurationRequest expectedUpdateRequest = updateRequestWith(functionArn, memorySizeMb);
        UpdateFunctionConfigurationResponse firstStatusResponse = updateResponseWith(LastUpdateStatus.IN_PROGRESS);
        GetFunctionConfigurationRequest getConfigRequest = getConfigRequestWith(functionArn);
        GetFunctionConfigurationResponse inProgressStatusResponse = getConfigResponseWith(LastUpdateStatus.IN_PROGRESS);
        GetFunctionConfigurationResponse successfulStatusResponse = getConfigResponseWith(LastUpdateStatus.SUCCESSFUL);

        when(awsProps.lambdaUpdateWaitSeconds()).thenReturn(1);
        when(lambdaClient.updateFunctionConfiguration(expectedUpdateRequest)).thenReturn(firstStatusResponse);
        when(lambdaClient.getFunctionConfiguration(getConfigRequest))
                .thenReturn(inProgressStatusResponse)
                .thenReturn(inProgressStatusResponse)
                .thenReturn(successfulStatusResponse);

        lambdaService.setFunctionMemory(functionArn, memorySizeMb);
        verify(lambdaClient).updateFunctionConfiguration(expectedUpdateRequest);
        verify(lambdaClient, times(3)).getFunctionConfiguration(getConfigRequest);
    }

    @Test
    public void Should_fail_if_reached_max_tries_for_get_config_when_function_update_in_progress() {
        String functionArn = generator.nextObject(String.class);
        int memorySizeMb = 128;
        UpdateFunctionConfigurationRequest expectedUpdateRequest = updateRequestWith(functionArn, memorySizeMb);
        UpdateFunctionConfigurationResponse firstStatusResponse = updateResponseWith(LastUpdateStatus.IN_PROGRESS);
        GetFunctionConfigurationRequest getConfigRequest = getConfigRequestWith(functionArn);
        GetFunctionConfigurationResponse inProgressStatusResponse = getConfigResponseWith(LastUpdateStatus.IN_PROGRESS);
        GetFunctionConfigurationResponse successfulStatusResponse = getConfigResponseWith(LastUpdateStatus.SUCCESSFUL);

        when(awsProps.lambdaUpdateWaitSeconds()).thenReturn(1);
        when(lambdaClient.updateFunctionConfiguration(expectedUpdateRequest)).thenReturn(firstStatusResponse);
        when(lambdaClient.getFunctionConfiguration(getConfigRequest))
                .thenReturn(inProgressStatusResponse)
                .thenReturn(inProgressStatusResponse)
                .thenReturn(inProgressStatusResponse)
                .thenReturn(successfulStatusResponse);

        Throwable thrown = assertThrows(LambdaUpdateMaxTriesException.class, () -> lambdaService.setFunctionMemory(functionArn, memorySizeMb));
        assertTrue(thrown.getMessage().contains(String.valueOf(memorySizeMb)));
        assertTrue(thrown.getMessage().contains(String.valueOf(functionArn)));
        verify(lambdaClient).updateFunctionConfiguration(expectedUpdateRequest);
        verify(lambdaClient, times(3)).getFunctionConfiguration(getConfigRequest);
    }

    @Test
    public void Should_fail_set_function_memory_if_less_than_128MB() {
        String functionArn = generator.nextObject(String.class);
        int memorySizeMb = 1;
        Throwable thrown = assertThrows(IllegalArgumentException.class, () -> lambdaService.setFunctionMemory(functionArn, memorySizeMb));
        assertTrue(thrown.getMessage().contains(String.valueOf(memorySizeMb)));
        verifyNoInteractions(lambdaClient);
    }

    @Test
    public void Should_fail_set_function_memory_if_higher_than_10240MB() {
        String functionArn = generator.nextObject(String.class);
        int memorySizeMb = 10241;
        Throwable thrown = assertThrows(IllegalArgumentException.class, () -> lambdaService.setFunctionMemory(functionArn, memorySizeMb));
        assertTrue(thrown.getMessage().contains(String.valueOf(memorySizeMb)));
        verifyNoInteractions(lambdaClient);
    }

    @Test
    public void Should_fail_when_unexpected_aws_sdk_exception_occurs_in_update() {
        String functionArn = generator.nextObject(String.class);
        int memorySizeMb = 10240;
        UpdateFunctionConfigurationRequest expectedUpdateRequest = updateRequestWith(functionArn, memorySizeMb);

        when(lambdaClient.updateFunctionConfiguration(expectedUpdateRequest)).thenThrow(SdkException.class);

        Throwable thrown = assertThrows(AwsOperationException.class, () -> lambdaService.setFunctionMemory(functionArn, memorySizeMb));
        assertTrue(thrown.getCause() instanceof SdkException);
        assertTrue(thrown.getMessage().contains(String.valueOf(memorySizeMb)));
        assertTrue(thrown.getMessage().contains(String.valueOf(functionArn)));
        verify(lambdaClient).updateFunctionConfiguration(expectedUpdateRequest);
        verify(lambdaClient, never()).getFunctionConfiguration(any(GetFunctionConfigurationRequest.class));
    }

    @Test
    public void Should_throw_lambda_not_found_when_not_found_in_update() {
        String functionArn = generator.nextObject(String.class);
        int memorySizeMb = 10240;
        UpdateFunctionConfigurationRequest expectedUpdateRequest = updateRequestWith(functionArn, memorySizeMb);

        when(lambdaClient.updateFunctionConfiguration(expectedUpdateRequest)).thenThrow(ResourceNotFoundException.class);

        Throwable thrown = assertThrows(LambdaNotFoundException.class, () -> lambdaService.setFunctionMemory(functionArn, memorySizeMb));
        verify(lambdaClient).updateFunctionConfiguration(expectedUpdateRequest);
        verify(lambdaClient, never()).getFunctionConfiguration(any(GetFunctionConfigurationRequest.class));
    }

    @Test
    public void Should_fail_when_unexpected_aws_sdk_exception_occurs_when_get_config() {
        String functionArn = generator.nextObject(String.class);
        int memorySizeMb = 10240;
        UpdateFunctionConfigurationRequest expectedUpdateRequest = updateRequestWith(functionArn, memorySizeMb);
        UpdateFunctionConfigurationResponse firstStatusResponse = updateResponseWith(LastUpdateStatus.IN_PROGRESS);
        GetFunctionConfigurationRequest expectedGetConfiRequest = getConfigRequestWith(functionArn);

        when(lambdaClient.updateFunctionConfiguration(expectedUpdateRequest)).thenReturn(firstStatusResponse);
        when(lambdaClient.getFunctionConfiguration(expectedGetConfiRequest)).thenThrow(SdkException.class);

        Throwable thrown = assertThrows(AwsOperationException.class, () -> lambdaService.setFunctionMemory(functionArn, memorySizeMb));
        assertTrue(thrown.getCause() instanceof SdkException);
        assertTrue(thrown.getMessage().contains(String.valueOf(memorySizeMb)));
        assertTrue(thrown.getMessage().contains(String.valueOf(functionArn)));
        verify(lambdaClient).updateFunctionConfiguration(expectedUpdateRequest);
        verify(lambdaClient).getFunctionConfiguration(expectedGetConfiRequest);
    }

    private GetFunctionConfigurationResponse getConfigResponseWith(LastUpdateStatus status) {
        return GetFunctionConfigurationResponse
                .builder()
                .lastUpdateStatus(status)
                .build();
    }

    private GetFunctionConfigurationRequest getConfigRequestWith(String functionArn) {
        return GetFunctionConfigurationRequest
                .builder()
                .functionName(functionArn)
                .build();
    }

    private UpdateFunctionConfigurationResponse updateResponseWith(LastUpdateStatus status) {
        return UpdateFunctionConfigurationResponse
                .builder()
                .lastUpdateStatus(status)
                .build();
    }
    private UpdateFunctionConfigurationRequest updateRequestWith(String functionArn, int memorySizeMb) {
        return UpdateFunctionConfigurationRequest
                .builder()
                .functionName(functionArn)
                .memorySize(memorySizeMb)
                .build();
    }
}