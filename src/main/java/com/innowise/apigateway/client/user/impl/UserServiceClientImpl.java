package com.innowise.apigateway.client.user.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.apigateway.client.BaseWebClient;
import com.innowise.apigateway.client.user.UserServiceClient;
import com.innowise.apigateway.client.user.dto.request.CreateUserRequestDto;
import com.innowise.apigateway.client.user.dto.response.UserResponseDto;
import com.innowise.apigateway.common.constants.paths.SecurityPaths;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class UserServiceClientImpl extends BaseWebClient implements UserServiceClient {

    @Qualifier("userWebClient")
    private final WebClient userWebClient;

    private static final String SERVICE_NAME = "user-service";

    public UserServiceClientImpl(@Qualifier("userWebClient") WebClient userWebClient, ObjectMapper objectMapper) {
        super(objectMapper, SERVICE_NAME);
        this.userWebClient = userWebClient;
    }

    @Override
    public Mono<UserResponseDto> createUser(CreateUserRequestDto request) {
        return withConnectionErrorHandling(
                userWebClient
                        .post()
                        .uri(SecurityPaths.INTERNAL_USERS)
                        .bodyValue(request)
                        .exchangeToMono(response -> handleResponse(response, UserResponseDto.class))
        );
    }

    @Override
    public Mono<Void> deleteUser(UUID userId) {
        return withConnectionErrorHandling(
                userWebClient
                        .delete()
                        .uri(SecurityPaths.INTERNAL_USERS_WITH_ID + userId)
                        .exchangeToMono(this::handleVoidResponse)
        );
    }
}