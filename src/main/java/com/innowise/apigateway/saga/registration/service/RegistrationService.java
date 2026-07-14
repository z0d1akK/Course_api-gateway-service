package com.innowise.apigateway.saga.registration.service;

import com.innowise.apigateway.client.auth.AuthServiceClient;
import com.innowise.apigateway.client.user.UserServiceClient;
import com.innowise.apigateway.common.exception.RollbackFailedException;
import com.innowise.apigateway.common.exception.ServiceCommunicationException;
import com.innowise.apigateway.saga.registration.dto.RegistrationRequestDto;
import com.innowise.apigateway.saga.registration.dto.RegistrationResponseDto;
import reactor.core.publisher.Mono;

public interface RegistrationService {

    /**
     * Executes the full registration saga for a new user.
     *
     * <p>This method orchestrates a multi-step registration process across distributed services.
     * The saga executes in the following order:</p>
     * <ol>
     *   <li><b>User Creation:</b> Creates a new user profile via {@code UserServiceClient#createUser(CreateUserRequestDto)}.
     *       If this step fails with a non-{@link ServiceCommunicationException}, it is wrapped
     *       into a {@link ServiceCommunicationException} with status {@code 500 INTERNAL_SERVER_ERROR}
     *       and the saga terminates immediately without rollback.</li>
     *   <li><b>Credentials Creation:</b> Saves user authentication credentials via
     *       {@code AuthServiceClient#saveCredentials(RegisterCredentialsRequestDto)}.
     *       If this step fails, the saga triggers a compensating transaction to delete
     *       the previously created user.</li>
     *   <li><b>Authentication:</b> Performs an initial login to generate JWT access and refresh tokens
     *       via {@code AuthServiceClient#login(LoginRequestDto)}. If this step fails,
     *       the saga triggers a compensating transaction.</li>
     * </ol>
     *
     * <p><b>Error Handling:</b></p>
     * <ul>
     *   <li>{@link ServiceCommunicationException} instances are propagated as-is without wrapping</li>
     *   <li>All other exceptions are wrapped into {@link ServiceCommunicationException} with
     *       appropriate error messages from {@code ErrorMessages}</li>
     *   <li>If rollback fails, a {@link RollbackFailedException} is thrown with the original
     *       exception as the suppressed error</li>
     * </ul>
     *
     * <p><b>Saga Compensation:</b> When step 2 or 3 fails, the service attempts to delete
     * the created user by calling {@code UserServiceClient#deleteUser(UUID)}. This ensures
     * that no orphaned user records remain in the system if the registration cannot be completed.</p>
     *
     * @param request the registration request containing user personal information
     *                and credentials. Must not be {@code null}. All fields are validated
     *                by the controller layer before reaching this service.
     * @return a {@link Mono} emitting the {@link RegistrationResponseDto} containing
     *         user ID, JWT access token, refresh token, and token type upon successful
     *         completion of all saga steps
     * @throws ServiceCommunicationException if any microservice communication fails
     * @throws RollbackFailedException if the compensating transaction (user deletion) fails
     * @see RegistrationRequestDto
     * @see RegistrationResponseDto
     * @see UserServiceClient#createUser
     * @see AuthServiceClient#saveCredentials
     * @see AuthServiceClient#login
     */
    Mono<RegistrationResponseDto> register(RegistrationRequestDto request);
}