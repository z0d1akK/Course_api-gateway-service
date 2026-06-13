package com.innowise.apigateway.client.user.impl;

import com.innowise.apigateway.client.BaseWebClient;
import com.innowise.apigateway.client.user.UserServiceClient;
import com.innowise.apigateway.client.user.dto.request.CreateUserRequestDto;
import com.innowise.apigateway.client.user.dto.response.UserResponseDto;
import com.innowise.apigateway.common.constants.ApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceClientImpl extends BaseWebClient implements UserServiceClient {

    @Qualifier("userWebClient")
    private final WebClient userWebClient;

    @Override
    public Mono<UserResponseDto> createUser(CreateUserRequestDto request) {

        return userWebClient
                .post()
                .uri(ApiPaths.INTERNAL_USERS)
                .bodyValue(request)
                .exchangeToMono(response -> handleResponse(response, UserResponseDto.class));
    }

    @Override
    public Mono<Void> deleteUser(UUID userId) {
        return userWebClient
                .delete()
                .uri(ApiPaths.INTERNAL_USERS_WITH_ID + userId)
                .exchangeToMono(this::handleVoidResponse);
    }
}