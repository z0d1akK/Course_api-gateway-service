package com.innowise.apigateway.integration.gateway;

import com.github.tomakehurst.wiremock.http.Fault;
import com.innowise.apigateway.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.innowise.apigateway.common.constants.Headers.USER_ID;

class GatewayExceptionHandlingIntegrationTest extends AbstractIntegrationTest {

    @Test
    void shouldReturnUnauthorizedWhenAuthServiceUnavailable() {

        wireMockServer.stubFor(post(urlEqualTo("/internal/auth/validate"))
                .willReturn(aResponse()
                        .withFault(Fault.CONNECTION_RESET_BY_PEER)));

        webTestClient.get()
                .uri("/api/orders")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer token"
                )
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void shouldReturn5xxWhenDownstreamServiceUnavailable() {

        wireMockServer.stubFor(post(urlEqualTo("/internal/auth/validate"))
                .willReturn(okJson("""
                    {
                        "valid": true,
                        "userId": "%s",
                        "role": "ROLE_USER",
                        "login": "user@test.com"
                    }
                    """.formatted(USER_ID))));

        wireMockServer.stubFor(get(urlEqualTo("/api/orders"))
                .willReturn(aResponse()
                        .withFault(Fault.CONNECTION_RESET_BY_PEER)));

        webTestClient.get()
                .uri("/api/orders")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer token"
                )
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

}
