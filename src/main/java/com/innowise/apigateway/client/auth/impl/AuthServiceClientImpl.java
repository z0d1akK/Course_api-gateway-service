package com.innowise.apigateway.client.auth.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.apigateway.client.BaseWebClient;
import com.innowise.apigateway.client.auth.AuthServiceClient;
import com.innowise.apigateway.client.auth.dto.request.LoginRequestDto;
import com.innowise.apigateway.client.auth.dto.request.RefreshTokenRequestDto;
import com.innowise.apigateway.client.auth.dto.request.RegisterCredentialsRequestDto;
import com.innowise.apigateway.client.auth.dto.request.ValidateTokenRequestDto;
import com.innowise.apigateway.client.auth.dto.response.AuthCredentialResponseDto;
import com.innowise.apigateway.client.auth.dto.response.TokenResponseDto;
import com.innowise.apigateway.client.auth.dto.response.TokenValidationResponseDto;
import com.innowise.apigateway.common.constants.paths.ApiPaths;
import com.innowise.apigateway.common.constants.paths.SecurityPaths;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class AuthServiceClientImpl extends BaseWebClient implements AuthServiceClient {

    @Qualifier("authWebClient")
    private final WebClient authWebClient;

    private static final String SERVICE_NAME = "auth-service";

    public AuthServiceClientImpl(@Qualifier("authWebClient") WebClient authWebClient, ObjectMapper objectMapper) {
        super(objectMapper, SERVICE_NAME);
        this.authWebClient = authWebClient;
    }

    @Override
    public Mono<AuthCredentialResponseDto> saveCredentials(RegisterCredentialsRequestDto request) {
        return withConnectionErrorHandling(
                authWebClient
                        .post()
                        .uri(SecurityPaths.INTERNAL_CREDENTIALS)
                        .bodyValue(request)
                        .exchangeToMono(response -> handleResponse(response, AuthCredentialResponseDto.class))
        );
    }

    @Override
    public Mono<TokenResponseDto> login(LoginRequestDto request) {
        return withConnectionErrorHandling(
                authWebClient
                        .post()
                        .uri(ApiPaths.LOGIN)
                        .bodyValue(request)
                        .exchangeToMono(response -> handleResponse(response, TokenResponseDto.class))
        );
    }

    @Override
    public Mono<TokenResponseDto> refresh(RefreshTokenRequestDto request) {
        return withConnectionErrorHandling(
                authWebClient
                        .post()
                        .uri(ApiPaths.REFRESH)
                        .bodyValue(request)
                        .exchangeToMono(response -> handleResponse(response, TokenResponseDto.class))
        );
    }

    @Override
    public Mono<TokenValidationResponseDto> validateToken(ValidateTokenRequestDto request) {
        return withConnectionErrorHandling(
                authWebClient
                        .post()
                        .uri(SecurityPaths.INTERNAL_VALIDATE)
                        .bodyValue(request)
                        .exchangeToMono(response -> handleResponse(response, TokenValidationResponseDto.class))
        );
    }
}