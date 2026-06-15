package com.innowise.apigateway.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.apigateway.common.constants.messages.ErrorMessages;
import com.innowise.apigateway.common.exception.ServiceCommunicationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.TimeoutException;

public abstract class BaseWebClient {

    private final ObjectMapper objectMapper;
    private final String serviceName;

    protected BaseWebClient(ObjectMapper objectMapper, String serviceName) {
        this.objectMapper = objectMapper;
        this.serviceName = serviceName;
    }

    protected <T> Mono<T> handleResponse(ClientResponse response, Class<T> bodyType) {
        if (response.statusCode().isError()) {
            return response.bodyToMono(String.class)
                    .defaultIfEmpty(ErrorMessages.UNKNOWN)
                    .flatMap(errorBody -> {
                        String errorMessage = parseErrorMessage(errorBody);
                        return Mono.error(
                                new ServiceCommunicationException(response.statusCode(), errorMessage));
                    });
        }
        return response.bodyToMono(bodyType);
    }

    protected Mono<Void> handleVoidResponse(ClientResponse response) {
        if (response.statusCode().isError()) {
            return response.bodyToMono(String.class)
                    .defaultIfEmpty(ErrorMessages.UNKNOWN)
                    .flatMap(errorBody -> {
                        String errorMessage = parseErrorMessage(errorBody);
                        return Mono.error(
                                new ServiceCommunicationException(response.statusCode(), errorMessage));
                    });
        }
        return Mono.empty();
    }

    protected <T> Mono<T> withConnectionErrorHandling(Mono<T> mono) {
        return mono.onErrorMap(this::mapConnectionError);
    }

    protected Throwable mapConnectionError(Throwable error) {
        if (error instanceof ServiceCommunicationException) {
            return error;
        }

        if (isConnectionError(error)) {
            String message = getServiceUnavailableMessage();
            return new ServiceCommunicationException(HttpStatus.SERVICE_UNAVAILABLE, message);
        }

        return error;
    }

    private boolean isConnectionError(Throwable error) {
        if (error instanceof ServiceCommunicationException) {
            return false;
        }

        Throwable cause = error;
        while (cause != null) {
            if (cause instanceof ConnectException ||
                    cause instanceof TimeoutException ||
                    cause instanceof ClosedChannelException) {
                return true;
            }

            if (cause.getMessage() != null) {
                String msg = cause.getMessage().toLowerCase();
                if (msg.contains("connection refused") ||
                        msg.contains("connection reset") ||
                        msg.contains("no route to host") ||
                        msg.contains("host is down") ||
                        msg.contains("connection timed out")) {
                    return true;
                }
            }
            cause = cause.getCause();
        }
        return false;
    }

    private String getServiceUnavailableMessage() {
        if (serviceName == null) {
            return ErrorMessages.INTERNAL_SERVER_ERROR;
        }

        return switch (serviceName.toLowerCase()) {
            case "auth-service" -> ErrorMessages.AUTH_SERVICE_UNAVAILABLE;
            case "user-service" -> ErrorMessages.USER_SERVICE_UNAVAILABLE;
            default -> serviceName + " is temporarily unavailable.";
        };
    }

    private String parseErrorMessage(String responseBody) {
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            if (jsonNode.has("message")) {
                String message = jsonNode.get("message").asText();
                if (message != null && message.trim().startsWith("{")) {
                    return parseErrorMessage(message);
                }
                return message;
            }

            if (jsonNode.has("error")) {
                return jsonNode.get("error").asText();
            }

            if (jsonNode.has("errors") && jsonNode.get("errors").isArray()) {
                StringBuilder sb = new StringBuilder();
                jsonNode.get("errors").forEach(error -> {
                    if (error.has("field") && error.has("message")) {
                        if (!sb.isEmpty()) {
                            sb.append("; ");
                        }
                        sb.append(error.get("field").asText())
                                .append(": ")
                                .append(error.get("message").asText());
                    }
                });
                if (!sb.isEmpty()) {
                    return sb.toString();
                }
            }

            return responseBody;
        } catch (Exception e) {
            return responseBody;
        }
    }
}