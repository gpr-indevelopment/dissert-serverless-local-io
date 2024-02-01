package io.github.gprindevelopment.dissertexporchestrator.aws;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.*;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class AwsService {

    private final LambdaClient lambdaClient;
    private final AwsProps awsProps;

    public void setFunctionMemory(String functionArn, int memorySizeMb) throws AwsOperationException {
        log.info("Setting function with ARN: {} to memory: {} MB", functionArn, memorySizeMb);
        validateAllowedMemorySize(memorySizeMb);
        try {
            UpdateFunctionConfigurationRequest configUpdateRequest = UpdateFunctionConfigurationRequest
                    .builder()
                    .functionName(functionArn)
                    .memorySize(memorySizeMb)
                    .build();
            UpdateFunctionConfigurationResponse configUpdateResponse = lambdaClient.updateFunctionConfiguration(configUpdateRequest);
            verifyUpdateSuccessful(functionArn, memorySizeMb, configUpdateResponse.lastUpdateStatus(), 0);
        } catch (SdkException ex) {
            String message = String.format("An SDK failure happened while updating Lambda function config with functionArn: %s and memorySize: %d MB",
                    functionArn,
                    memorySizeMb);
            log.error(message, ex);
            throw new AwsOperationException(message, ex);
        }
    }

    private void verifyUpdateSuccessful(
            String functionArn,
            int memorySizeMb,
            LastUpdateStatus status,
            int tryCount) throws AwsOperationException {
        log.debug("Verifying update for functionArn: {} and memorySize: {} MB for status: {} and tryCount for GetConfiguration: {}",
                functionArn,
                memorySizeMb,
                status,
                tryCount);
        if (status.equals(LastUpdateStatus.SUCCESSFUL)) {
            log.info("Successfully set function with ARN: {} to memory: {} MB", functionArn, memorySizeMb);
            return;
        }
        if (status.equals(LastUpdateStatus.FAILED)) {
            String message = String.format("Update status FAILED for functionArn %s and memorySize %d MB", functionArn, memorySizeMb);
            throw new AwsOperationException(message);
        }
        if (tryCount >= 3) {
            String message = String.format("Reached the maximum number of %d tries for updating functionArn %s and memorySize %d MB",
                    tryCount,
                    functionArn,
                    memorySizeMb);
            throw new AwsOperationException(message);
        }
        waitForUpdate(functionArn, memorySizeMb, Duration.ofSeconds(awsProps.lambdaUpdateWaitSeconds()));
        GetFunctionConfigurationRequest getConfigRequest = GetFunctionConfigurationRequest
                .builder()
                .functionName(functionArn)
                .build();
        GetFunctionConfigurationResponse getConfigResponse = lambdaClient.getFunctionConfiguration(getConfigRequest);
        verifyUpdateSuccessful(functionArn, memorySizeMb, getConfigResponse.lastUpdateStatus(), tryCount + 1);
    }

    private void waitForUpdate(String functionArn, int memorySizeMb, Duration duration) {
        log.debug("Update for Lambda function is not ready, will wait {} millis and retry. FunctionArn: {}, memorySize: {} MB",
                duration.toMillis(),
                functionArn,
                memorySizeMb);
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            throw new RuntimeException(String.format("Was not able to wait for %d millis.", duration.toMillis()), e);
        }
    }

    private void validateAllowedMemorySize(int memorySizeMb) {
        if (memorySizeMb < 128) {
            String message = String.format("Lambda functions cannot have memory lower than 128 MB. Input was %d MB.", memorySizeMb);
            throw new IllegalArgumentException(message);
        }
        if (memorySizeMb > 10240) {
            String message = String.format("Lambda functions cannot have memory higher than 10240 MB. Input was %d MB.", memorySizeMb);
            throw new IllegalArgumentException(message);
        }
    }
}
