package com.innowise.apigateway.saga.registration.service.impl;

import com.innowise.apigateway.client.auth.AuthServiceClient;
import com.innowise.apigateway.client.auth.dto.request.LoginRequestDto;
import com.innowise.apigateway.client.auth.dto.request.RegisterCredentialsRequestDto;
import com.innowise.apigateway.client.user.UserServiceClient;
import com.innowise.apigateway.client.user.dto.request.CreateUserRequestDto;
import com.innowise.apigateway.client.user.dto.response.UserResponseDto;
import com.innowise.apigateway.common.constants.messages.ErrorMessages;
import com.innowise.apigateway.common.exception.RollbackFailedException;
import com.innowise.apigateway.common.exception.ServiceCommunicationException;
import com.innowise.apigateway.saga.registration.dto.RegistrationRequestDto;
import com.innowise.apigateway.saga.registration.dto.RegistrationResponseDto;
import com.innowise.apigateway.saga.registration.mapper.RegistrationMapper;
import com.innowise.apigateway.saga.registration.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final UserServiceClient userServiceClient;

    private final AuthServiceClient authServiceClient;

    private final RegistrationMapper mapper;

    @Override
    public Mono<RegistrationResponseDto> register(RegistrationRequestDto request) {
        CreateUserRequestDto createUserRequest = mapper.toCreateUserRequest(request);

        return userServiceClient.createUser(createUserRequest)
                .onErrorMap(error -> {
                    if (error instanceof ServiceCommunicationException) {
                        return error;
                    }
                    return new ServiceCommunicationException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessages.REGISTRATION_FAILED);
                })
                .flatMap(user -> createCredentials(request, user));
    }

    private Mono<RegistrationResponseDto> createCredentials(RegistrationRequestDto request, UserResponseDto user) {
        RegisterCredentialsRequestDto credentialsRequest = mapper.toRegisterCredentialsRequest(request, user.getId());

        return authServiceClient.saveCredentials(credentialsRequest)
                .onErrorMap(error -> {
                    if (error instanceof ServiceCommunicationException) {
                        return error;
                    }
                    return new ServiceCommunicationException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessages.CREDENTIALS_CREATION_FAILED);
                })
                .flatMap(credentials -> login(request, user))
                .onErrorResume(ex -> rollbackUser(user.getId()).then(Mono.error(ex)));
    }

    private Mono<RegistrationResponseDto> login(RegistrationRequestDto request, UserResponseDto user) {
        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .login(request.getEmail())
                .password(request.getPassword())
                .build();

        return authServiceClient.login(loginRequest)
                .onErrorMap(error -> {
                    if (error instanceof ServiceCommunicationException) {
                        return error;
                    }
                    return new ServiceCommunicationException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessages.TOKEN_VALIDATION_FAILED);
                })
                .map(token -> mapper.toRegistrationResponse(token, user.getId()));
    }

    private Mono<Void> rollbackUser(UUID userId) {
        return userServiceClient.deleteUser(userId)
                .onErrorMap(error -> new RollbackFailedException(
                        ErrorMessages.USER_ROLLBACK_FAILED + " for userId: " + userId, error));
    }
}