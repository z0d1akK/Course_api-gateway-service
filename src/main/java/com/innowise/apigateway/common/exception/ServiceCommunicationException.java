package com.innowise.apigateway.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class ServiceCommunicationException extends RuntimeException {

    private final HttpStatusCode status;

    public ServiceCommunicationException(HttpStatusCode status, String message) {
        super(message);
        this.status = status;
    }
}