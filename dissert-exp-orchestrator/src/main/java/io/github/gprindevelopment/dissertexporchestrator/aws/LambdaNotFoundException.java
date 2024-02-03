package io.github.gprindevelopment.dissertexporchestrator.aws;

public class LambdaNotFoundException extends Exception {

    public LambdaNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
