package com.innowise.apigateway.unit.saga;

import com.innowise.apigateway.client.auth.AuthServiceClient;
import com.innowise.apigateway.client.auth.dto.request.LoginRequestDto;
import com.innowise.apigateway.client.auth.dto.request.RegisterCredentialsRequestDto;
import com.innowise.apigateway.client.auth.dto.response.AuthCredentialResponseDto;
import com.innowise.apigateway.client.auth.dto.response.TokenResponseDto;
import com.innowise.apigateway.client.user.UserServiceClient;
import com.innowise.apigateway.client.user.dto.request.CreateUserRequestDto;
import com.innowise.apigateway.client.user.dto.response.UserResponseDto;
import com.innowise.apigateway.common.constants.messages.ErrorMessages;
import com.innowise.apigateway.common.exception.RollbackFailedException;
import com.innowise.apigateway.common.exception.ServiceCommunicationException;
import com.innowise.apigateway.saga.registration.dto.RegistrationRequestDto;
import com.innowise.apigateway.saga.registration.dto.RegistrationResponseDto;
import com.innowise.apigateway.saga.registration.mapper.RegistrationMapper;
import com.innowise.apigateway.saga.registration.service.impl.RegistrationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationSagaServiceTest {

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private AuthServiceClient authServiceClient;

    @Mock
    private RegistrationMapper mapper;

    @InjectMocks
    private RegistrationServiceImpl registrationService;

    private RegistrationRequestDto request;

    private UserResponseDto userResponse;

    private CreateUserRequestDto createUserRequest;

    private RegisterCredentialsRequestDto credentialsRequest;

    private TokenResponseDto tokenResponse;

    private RegistrationResponseDto expectedResponse;

    private static final UUID USER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        request = RegistrationRequestDto.builder()
                .name("test")
                .surname("test")
                .birthDate(LocalDate.of(1990, 1, 1))
                .email("test@example.com")
                .password("password123")
                .build();

        createUserRequest = CreateUserRequestDto.builder()
                .name("test")
                .surname("test")
                .birthDate(LocalDate.of(1990, 1, 1))
                .email("test@example.com")
                .build();

        userResponse = UserResponseDto.builder()
                .id(USER_ID)
                .name("test")
                .surname("test")
                .email("test@example.com")
                .active(true)
                .build();

        credentialsRequest = RegisterCredentialsRequestDto.builder()
                .userId(USER_ID)
                .login("test@example.com")
                .password("password123")
                .build();

        tokenResponse = TokenResponseDto.builder()
                .accessToken("access.token")
                .refreshToken("refresh.token")
                .build();

        expectedResponse = RegistrationResponseDto.builder()
                .userId(USER_ID)
                .accessToken("access.token")
                .refreshToken("refresh.token")
                .tokenType("Bearer")
                .build();
    }

    @Nested
    @DisplayName("Successful registration")
    class SuccessfulRegistration {

        @Test
        @DisplayName("Should complete full registration saga successfully")
        void shouldCompleteRegistrationSuccessfully() {
            when(mapper.toCreateUserRequest(request)).thenReturn(createUserRequest);
            when(userServiceClient.createUser(createUserRequest)).thenReturn(Mono.just(userResponse));
            when(mapper.toRegisterCredentialsRequest(request, USER_ID)).thenReturn(credentialsRequest);
            when(authServiceClient.saveCredentials(any())).thenReturn(Mono.just(new AuthCredentialResponseDto()));
            when(authServiceClient.login(any(LoginRequestDto.class))).thenReturn(Mono.just(tokenResponse));
            when(mapper.toRegistrationResponse(tokenResponse, USER_ID)).thenReturn(expectedResponse);

            var result = registrationService.register(request);

            StepVerifier.create(result)
                    .expectNext(expectedResponse)
                    .verifyComplete();

            verify(userServiceClient).createUser(createUserRequest);
            verify(authServiceClient).saveCredentials(any());
            verify(authServiceClient).login(any());
            verify(userServiceClient, never()).deleteUser(any());
        }
    }

    @Nested
    @DisplayName("User creation failures")
    class UserCreationFailures {

        @Test
        @DisplayName("Should propagate ServiceCommunicationException when user creation fails")
        void shouldPropagateServiceCommunicationExceptionWhenUserCreationFails() {
            when(mapper.toCreateUserRequest(request)).thenReturn(createUserRequest);
            when(userServiceClient.createUser(createUserRequest))
                    .thenReturn(Mono.error(new ServiceCommunicationException(
                            HttpStatus.BAD_REQUEST, "User already exists")));

            var result = registrationService.register(request);

            StepVerifier.create(result)
                    .expectErrorMatches(error ->
                            error instanceof ServiceCommunicationException &&
                                    ((ServiceCommunicationException) error).getStatus() == HttpStatus.BAD_REQUEST &&
                                    error.getMessage().equals("User already exists"))
                    .verify();

            verify(authServiceClient, never()).saveCredentials(any());
            verify(userServiceClient, never()).deleteUser(any());
        }

        @Test
        @DisplayName("Should wrap non-ServiceCommunicationException when user creation fails")
        void shouldWrapNonServiceCommunicationExceptionWhenUserCreationFails() {
            when(mapper.toCreateUserRequest(request)).thenReturn(createUserRequest);
            when(userServiceClient.createUser(createUserRequest))
                    .thenReturn(Mono.error(new RuntimeException("Connection timeout")));

            var result = registrationService.register(request);

            StepVerifier.create(result)
                    .expectErrorMatches(error ->
                            error instanceof ServiceCommunicationException &&
                                    ((ServiceCommunicationException) error).getStatus() == HttpStatus.INTERNAL_SERVER_ERROR &&
                                    error.getMessage().equals(ErrorMessages.REGISTRATION_FAILED))
                    .verify();

            verify(authServiceClient, never()).saveCredentials(any());
            verify(userServiceClient, never()).deleteUser(any());
        }
    }

    @Nested
    @DisplayName("Credentials creation failures")
    class CredentialsCreationFailures {

        @Test
        @DisplayName("Should rollback user when credentials creation fails with ServiceCommunicationException")
        void shouldRollbackWhenCredentialsCreationFailsWithServiceCommunicationException() {
            when(mapper.toCreateUserRequest(request)).thenReturn(createUserRequest);
            when(userServiceClient.createUser(createUserRequest)).thenReturn(Mono.just(userResponse));
            when(mapper.toRegisterCredentialsRequest(request, USER_ID)).thenReturn(credentialsRequest);
            when(authServiceClient.saveCredentials(any()))
                    .thenReturn(Mono.error(new ServiceCommunicationException(
                            HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create credentials")));
            when(userServiceClient.deleteUser(USER_ID)).thenReturn(Mono.empty());

            var result = registrationService.register(request);

            StepVerifier.create(result)
                    .expectErrorMatches(error ->
                            error instanceof ServiceCommunicationException &&
                                    error.getMessage().equals("Failed to create credentials"))
                    .verify();

            verify(userServiceClient).deleteUser(USER_ID);
        }

        @Test
        @DisplayName("Should rollback user when credentials creation fails with unexpected error")
        void shouldRollbackWhenCredentialsCreationFailsWithUnexpectedError() {
            when(mapper.toCreateUserRequest(request)).thenReturn(createUserRequest);
            when(userServiceClient.createUser(createUserRequest)).thenReturn(Mono.just(userResponse));
            when(mapper.toRegisterCredentialsRequest(request, USER_ID)).thenReturn(credentialsRequest);
            when(authServiceClient.saveCredentials(any()))
                    .thenReturn(Mono.error(new RuntimeException("Unexpected error")));
            when(userServiceClient.deleteUser(USER_ID)).thenReturn(Mono.empty());

            var result = registrationService.register(request);

            StepVerifier.create(result)
                    .expectErrorMatches(error ->
                            error instanceof ServiceCommunicationException &&
                                    ((ServiceCommunicationException) error).getStatus() == HttpStatus.INTERNAL_SERVER_ERROR &&
                                    error.getMessage().equals(ErrorMessages.CREDENTIALS_CREATION_FAILED))
                    .verify();

            verify(userServiceClient).deleteUser(USER_ID);
        }
    }

    @Nested
    @DisplayName("Login failures")
    class LoginFailures {

        @Test
        @DisplayName("Should rollback user when login fails with ServiceCommunicationException")
        void shouldRollbackWhenLoginFailsWithServiceCommunicationException() {
            when(mapper.toCreateUserRequest(request)).thenReturn(createUserRequest);
            when(userServiceClient.createUser(createUserRequest)).thenReturn(Mono.just(userResponse));
            when(mapper.toRegisterCredentialsRequest(request, USER_ID)).thenReturn(credentialsRequest);
            when(authServiceClient.saveCredentials(any())).thenReturn(Mono.just(new AuthCredentialResponseDto()));
            when(authServiceClient.login(any(LoginRequestDto.class)))
                    .thenReturn(Mono.error(new ServiceCommunicationException(
                            HttpStatus.UNAUTHORIZED, "Invalid credentials")));
            when(userServiceClient.deleteUser(USER_ID)).thenReturn(Mono.empty());

            var result = registrationService.register(request);

            StepVerifier.create(result)
                    .expectErrorMatches(error ->
                            error instanceof ServiceCommunicationException &&
                                    error.getMessage().equals("Invalid credentials"))
                    .verify();

            verify(userServiceClient).deleteUser(USER_ID);
        }

        @Test
        @DisplayName("Should rollback user when login fails with unexpected error")
        void shouldRollbackWhenLoginFailsWithUnexpectedError() {
            when(mapper.toCreateUserRequest(request)).thenReturn(createUserRequest);
            when(userServiceClient.createUser(createUserRequest)).thenReturn(Mono.just(userResponse));
            when(mapper.toRegisterCredentialsRequest(request, USER_ID)).thenReturn(credentialsRequest);
            when(authServiceClient.saveCredentials(any())).thenReturn(Mono.just(new AuthCredentialResponseDto()));
            when(authServiceClient.login(any(LoginRequestDto.class)))
                    .thenReturn(Mono.error(new RuntimeException("Connection refused")));
            when(userServiceClient.deleteUser(USER_ID)).thenReturn(Mono.empty());

            var result = registrationService.register(request);

            StepVerifier.create(result)
                    .expectErrorMatches(error ->
                            error instanceof ServiceCommunicationException &&
                                    ((ServiceCommunicationException) error).getStatus() == HttpStatus.INTERNAL_SERVER_ERROR &&
                                    error.getMessage().equals(ErrorMessages.TOKEN_VALIDATION_FAILED))
                    .verify();

            verify(userServiceClient).deleteUser(USER_ID);
        }
    }

    @Nested
    @DisplayName("Rollback failures")
    class RollbackFailures {

        @Test
        @DisplayName("Should throw RollbackFailedException when rollback itself fails")
        void shouldThrowRollbackFailedExceptionWhenRollbackFails() {
            when(mapper.toCreateUserRequest(request)).thenReturn(createUserRequest);
            when(userServiceClient.createUser(createUserRequest)).thenReturn(Mono.just(userResponse));
            when(mapper.toRegisterCredentialsRequest(request, USER_ID)).thenReturn(credentialsRequest);
            when(authServiceClient.saveCredentials(any()))
                    .thenReturn(Mono.error(new ServiceCommunicationException(
                            HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create credentials")));
            when(userServiceClient.deleteUser(USER_ID))
                    .thenReturn(Mono.error(new RuntimeException("Delete user failed")));

            var result = registrationService.register(request);

            StepVerifier.create(result)
                    .expectErrorMatches(error ->
                            error instanceof RollbackFailedException &&
                                    error.getMessage().contains(ErrorMessages.USER_ROLLBACK_FAILED) &&
                                    error.getMessage().contains(USER_ID.toString()))
                    .verify();

            verify(userServiceClient).deleteUser(USER_ID);
        }

        @Test
        @DisplayName("Should throw RollbackFailedException when rollback fails with ServiceCommunicationException")
        void shouldThrowRollbackFailedExceptionWhenRollbackFailsWithServiceCommunicationException() {
            when(mapper.toCreateUserRequest(request)).thenReturn(createUserRequest);
            when(userServiceClient.createUser(createUserRequest)).thenReturn(Mono.just(userResponse));
            when(mapper.toRegisterCredentialsRequest(request, USER_ID)).thenReturn(credentialsRequest);
            when(authServiceClient.saveCredentials(any()))
                    .thenReturn(Mono.error(new RuntimeException("Unexpected")));
            when(userServiceClient.deleteUser(USER_ID))
                    .thenReturn(Mono.error(new ServiceCommunicationException(
                            HttpStatus.SERVICE_UNAVAILABLE, "User service unavailable")));

            var result = registrationService.register(request);

            StepVerifier.create(result)
                    .expectErrorMatches(error ->
                            error instanceof RollbackFailedException &&
                                    error.getMessage().contains(ErrorMessages.USER_ROLLBACK_FAILED) &&
                                    error.getCause() instanceof ServiceCommunicationException)
                    .verify();
        }
    }
}