package com.innowise.apigateway.client;

import com.innowise.apigateway.common.constants.ErrorMessages;
import com.innowise.apigateway.common.exception.ServiceCommunicationException;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

public abstract class BaseWebClient {

    protected <T> Mono<T> handleResponse(ClientResponse response, Class<T> bodyType) {
        if (response.statusCode().isError()) {
            return response.bodyToMono(String.class)
                    .defaultIfEmpty(ErrorMessages.UNKNOWN)
                    .flatMap(errorBody ->
                            Mono.error(
                                    new ServiceCommunicationException(response.statusCode(), errorBody))
                    );
        }
        return response.bodyToMono(bodyType);
    }

    protected Mono<Void> handleVoidResponse(ClientResponse response) {
        if (response.statusCode().isError()) {
            return response.bodyToMono(String.class)
                    .defaultIfEmpty(ErrorMessages.UNKNOWN)
                    .flatMap(errorBody -> Mono.error(
                            new ServiceCommunicationException(response.statusCode(), errorBody)));
        }
        return Mono.empty();
    }
}