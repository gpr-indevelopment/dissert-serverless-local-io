package io.github.gprindevelopment.dissertexporchestrator.aws;

public class AwsOperationException extends RuntimeException {

    public AwsOperationException(String message) {
        super(message);
    }

    public AwsOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
