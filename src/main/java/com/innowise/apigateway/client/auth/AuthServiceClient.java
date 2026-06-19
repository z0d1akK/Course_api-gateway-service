package com.innowise.apigateway.client.auth;

import com.innowise.apigateway.client.auth.dto.request.LoginRequestDto;
import com.innowise.apigateway.client.auth.dto.request.RegisterCredentialsRequestDto;
import com.innowise.apigateway.client.auth.dto.request.ValidateTokenRequestDto;
import com.innowise.apigateway.client.auth.dto.response.AuthCredentialResponseDto;
import com.innowise.apigateway.client.auth.dto.response.TokenResponseDto;
import com.innowise.apigateway.client.auth.dto.response.TokenValidationResponseDto;
import reactor.core.publisher.Mono;

public interface AuthServiceClient {

    /**
     * Saves user credentials in the system after successful registration.
     *
     * @param request request object containing user registration credentials
     * @return response containing saved credential information
     */
    Mono<AuthCredentialResponseDto> saveCredentials(RegisterCredentialsRequestDto request);

    /**
     * Creates new access and refresh tokens based on provided login credentials.
     *
     * @param request request object containing login credentials
     * @return response containing access and refresh tokens
     */
    Mono<TokenResponseDto> login(LoginRequestDto request);

    /**
     * Validates provided access token and returns its metadata.
     *
     * @param request request object containing token to validate
     * @return response containing token validation result and metadata
     */
    Mono<TokenValidationResponseDto> validateToken(ValidateTokenRequestDto request);
}