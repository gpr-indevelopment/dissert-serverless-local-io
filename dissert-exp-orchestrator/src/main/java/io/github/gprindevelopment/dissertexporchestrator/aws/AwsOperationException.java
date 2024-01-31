package io.github.gprindevelopment.dissertexporchestrator.aws;

public class AwsOperationException extends Exception {

    public AwsOperationException(String message) {
        super(message);
    }

    public AwsOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
