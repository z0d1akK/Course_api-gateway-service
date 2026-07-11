package com.innowise.apigateway.integration.saga;

import com.innowise.apigateway.testclasses.RegistrationFactory;
import com.innowise.apigateway.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;


import static com.github.tomakehurst.wiremock.client.WireMock.*;

class RegistrationSagaIntegrationTest extends AbstractIntegrationTest {

    @Nested
    @DisplayName("Successful registration")
    class SuccessfulRegistration {

        @Test
        @DisplayName("Should complete full registration flow")
        void shouldCompleteFullRegistration() {
            wireMockServer.stubFor(post(urlEqualTo("/internal/users"))
                    .willReturn(aResponse()
                            .withStatus(201)
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withBody("""
                                    {
                                        "id": "e73dcc73-e1db-4c3a-9246-0e1c2de79074",
                                        "name": "test",
                                        "surname": "test",
                                        "birthDate": "1990-01-01",
                                        "email": "test@example.com",
                                        "active": true
                                    }
                                    """)));

            wireMockServer.stubFor(post(urlEqualTo("/internal/auth/credentials"))
                    .willReturn(aResponse()
                            .withStatus(201)
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withBody("{}")));

            wireMockServer.stubFor(post(urlEqualTo("/api/auth/token"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withBody("""
                                    {
                                        "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                                        "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
                                        "tokenType": "Bearer"
                                    }
                                    """)));

            var request = RegistrationFactory.createValidRequest();

            webTestClient.post().uri("/api/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody()
                    .jsonPath("$.userId").isNotEmpty()
                    .jsonPath("$.accessToken").isNotEmpty()
                    .jsonPath("$.refreshToken").isNotEmpty()
                    .jsonPath("$.tokenType").isEqualTo("Bearer");
        }
    }

    @Nested
    @DisplayName("Validation errors")
    class ValidationErrors {

        @Test
        @DisplayName("Should return 400 for invalid email")
        void shouldReturn400ForInvalidEmail() {
            var request = RegistrationFactory.createRequestWithInvalidEmail();

            webTestClient.post().uri("/api/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.errors[0].field").isEqualTo("email");
        }

        @Test
        @DisplayName("Should return 400 for empty name")
        void shouldReturn400ForEmptyName() {
            var request = RegistrationFactory.createRequestWithEmptyName();

            webTestClient.post().uri("/api/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isBadRequest();
        }
    }

    @Nested
    @DisplayName("Rollback scenarios")
    class RollbackScenarios {

        @Test
        @DisplayName("Should rollback when auth service fails after user creation")
        void shouldRollbackWhenAuthServiceFails() {
            wireMockServer.stubFor(post(urlEqualTo("/internal/users"))
                    .willReturn(aResponse()
                            .withStatus(201)
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withBody("""
                                    {
                                        "id": "e73dcc73-e1db-4c3a-9246-0e1c2de79074",
                                        "name": "test",
                                        "surname": "test",
                                        "email": "test@example.com",
                                        "active": true
                                    }
                                    """)));

            wireMockServer.stubFor(post(urlEqualTo("/internal/auth/credentials"))
                    .willReturn(aResponse()
                            .withStatus(500)
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withBody("""
                                    {
                                        "message": "Internal server error"
                                    }
                                    """)));

            wireMockServer.stubFor(delete(urlMatching("/internal/users/.*"))
                    .willReturn(aResponse().withStatus(204)));

            var request = RegistrationFactory.createValidRequest();

            webTestClient.post().uri("/api/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().is5xxServerError();

            wireMockServer.verify(deleteRequestedFor(urlMatching("/internal/users/.*")));
        }

        @Test
        @DisplayName("Should return error when user creation fails")
        void shouldReturnErrorWhenUserCreationFails() {
            wireMockServer.stubFor(post(urlEqualTo("/internal/users"))
                    .willReturn(aResponse()
                            .withStatus(400)
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withBody("""
                                    {
                                        "message": "User already exists"
                                    }
                                    """)));

            var request = RegistrationFactory.createValidRequest();

            webTestClient.post().uri("/api/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isBadRequest();
        }
    }
}