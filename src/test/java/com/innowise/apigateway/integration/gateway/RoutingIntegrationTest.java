package com.innowise.apigateway.integration.gateway;

import com.innowise.apigateway.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

class RoutingIntegrationTest extends AbstractIntegrationTest {

    @Nested
    @DisplayName("Public routes")
    class PublicRoutes {

        @Test
        @DisplayName("Should route to auth service login endpoint")
        void shouldRouteToAuthLogin() {
            wireMockServer.stubFor(post(urlEqualTo("/api/auth/token"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withBody("""
                                    {
                                        "accessToken": "token",
                                        "refreshToken": "refresh",
                                        "tokenType": "Bearer"
                                    }
                                    """)));

            webTestClient.post()
                    .uri("/api/auth/token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                                "login": "test@example.com",
                                "password": "password"
                            }
                            """)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.accessToken").isEqualTo("token")
                    .jsonPath("$.tokenType").isEqualTo("Bearer");
        }

        @Test
        @DisplayName("Should route to Swagger UI")
        void shouldRouteToSwaggerUI() {
            webTestClient.get()
                    .uri("/swagger-ui/index.html")
                    .exchange()
                    .expectStatus().isOk();
        }
    }

    @Nested
    @DisplayName("Protected routes")
    class ProtectedRoutes {

        @Test
        @DisplayName("Should return 401 when no token provided")
        void shouldReturn401WhenNoToken() {
            webTestClient.get()
                    .uri("/api/orders")
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("Should route to order service with valid token")
        void shouldRouteToOrderServiceWithValidToken() {
            wireMockServer.stubFor(post(urlEqualTo("/internal/auth/validate"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withBody("""
                                    {
                                        "valid": true,
                                        "userId": "e73dcc73-e1db-4c3a-9246-0e1c2de79074",
                                        "role": "ROLE_USER",
                                        "login": "test@example.com"
                                    }
                                    """)));

            wireMockServer.stubFor(get(urlEqualTo("/api/orders"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withBody("[]")));

            webTestClient.get()
                    .uri("/api/orders")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer valid.jwt.token")
                    .exchange()
                    .expectStatus().isOk();
        }
    }

    @Nested
    @DisplayName("Internal routes")
    class InternalRoutes {

        @Test
        @DisplayName("Should add internal API key to internal requests")
        void shouldAddInternalApiKeyToInternalRequests() {
            wireMockServer.stubFor(post(urlEqualTo("/internal/users"))
                    .withHeader("X-Internal-Key", equalTo("test-internal-key"))
                    .willReturn(aResponse()
                            .withStatus(201)
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withBody("{}")));

            webTestClient.post()
                    .uri("/internal/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{}")
                    .exchange()
                    .expectStatus().isEqualTo(201);
        }
    }
}