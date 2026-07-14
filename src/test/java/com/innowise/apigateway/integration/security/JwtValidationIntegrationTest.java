package com.innowise.apigateway.integration.security;

import com.innowise.apigateway.common.constants.Headers;
import com.innowise.apigateway.common.constants.SecurityConstants;
import com.innowise.apigateway.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

class JwtValidationIntegrationTest extends AbstractIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String VALID_TOKEN = "valid.jwt.token";

    @Nested
    @DisplayName("Token validation scenarios")
    class TokenValidation {

        @Test
        @DisplayName("Should pass user headers to downstream service on successful validation")
        void shouldPassUserHeadersOnSuccessfulValidation() {
            wireMockServer.stubFor(post(urlEqualTo("/internal/auth/validate"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withBody(String.format("""
                                    {
                                        "valid": true,
                                        "userId": "%s",
                                        "role": "ROLE_USER",
                                        "login": "test@example.com"
                                    }
                                    """, USER_ID))));

            wireMockServer.stubFor(get(urlEqualTo("/api/users/" + USER_ID))
                    .withHeader(Headers.USER_ID, equalTo(USER_ID.toString()))
                    .withHeader(Headers.USER_ROLE, equalTo("ROLE_USER"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withBody("{}")));

            webTestClient.get()
                    .uri("/api/users/" + USER_ID)
                    .header(HttpHeaders.AUTHORIZATION, SecurityConstants.BEARER_PREFIX + VALID_TOKEN)
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("Should pass user login headers to downstream service")
        void shouldPassUserLoginHeader() {
            wireMockServer.stubFor(post(urlEqualTo("/internal/auth/validate"))
                    .willReturn(okJson("""
                    {
                        "valid": true,
                        "userId": "%s",
                        "role": "ROLE_USER",
                        "login": "test@example.com"
                    }
                    """.formatted(USER_ID))));

            wireMockServer.stubFor(get(urlEqualTo("/api/users/" + USER_ID))
                    .withHeader(
                            Headers.USER_LOGIN,
                            equalTo("test@example.com")
                    )
                    .willReturn(okJson("{}")));

            webTestClient.get()
                    .uri("/api/users/" + USER_ID)
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer valid.token"
                    )
                    .exchange()
                    .expectStatus()
                    .isOk();
        }

        @Test
        @DisplayName("Should return 401 when Authorization header is missing")
        void shouldReturn401WhenAuthHeaderMissing() {
            webTestClient.get()
                    .uri("/api/orders")
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("Should return 401 when Authorization header format is invalid")
        void shouldReturn401WhenAuthHeaderInvalidFormat() {
            webTestClient.get()
                    .uri("/api/orders")
                    .header(HttpHeaders.AUTHORIZATION, "InvalidFormat token")
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("Should return 401 when token validation fails")
        void shouldReturn401WhenTokenInvalid() {
            wireMockServer.stubFor(post(urlEqualTo("/internal/auth/validate"))
                    .willReturn(aResponse()
                            .withStatus(401)
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withBody("""
                                    {
                                        "message": "Invalid token"
                                    }
                                    """)));

            webTestClient.get()
                    .uri("/api/orders")
                    .header(HttpHeaders.AUTHORIZATION, SecurityConstants.BEARER_PREFIX + "expired.token")
                    .exchange()
                    .expectStatus().isUnauthorized();
        }
    }

    @Nested
    @DisplayName("Role-based access")
    class RoleBasedAccess {

        @Test
        @DisplayName("Should pass admin role header correctly")
        void shouldPassAdminRoleHeader() {
            wireMockServer.stubFor(post(urlEqualTo("/internal/auth/validate"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withBody(String.format("""
                                    {
                                        "valid": true,
                                        "userId": "%s",
                                        "role": "ROLE_ADMIN",
                                        "login": "admin@example.com"
                                    }
                                    """, USER_ID))));

            wireMockServer.stubFor(get(urlEqualTo("/api/users"))
                    .withHeader(Headers.USER_ROLE, equalTo("ROLE_ADMIN"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withBody("[]")));

            webTestClient.get()
                    .uri("/api/users")
                    .header(HttpHeaders.AUTHORIZATION, SecurityConstants.BEARER_PREFIX + VALID_TOKEN)
                    .exchange()
                    .expectStatus().isOk();
        }
    }
}