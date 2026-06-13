package com.innowise.apigateway.client.user;

import com.innowise.apigateway.client.user.dto.request.CreateUserRequestDto;
import com.innowise.apigateway.client.user.dto.response.UserResponseDto;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserServiceClient {

    /**
     * Creates a new user.
     *
     * @param request request object containing user creation data
     * @return created user response
     */
    Mono<UserResponseDto> createUser(CreateUserRequestDto request);

    /**
     * Deletes user by identifier.
     *
     * @param userId user identifier
     */
    Mono<Void> deleteUser(UUID userId);
}