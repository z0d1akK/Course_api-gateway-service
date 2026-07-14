package com.innowise.apigateway.common.exception;

public class RollbackFailedException extends RuntimeException {

    public RollbackFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}