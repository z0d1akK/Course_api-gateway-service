package com.innowise.apigateway.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.innowise.apigateway.config.WireMockConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(WireMockConfiguration.class)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    static {
        WireMockConfiguration.getInstance();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        int port = WireMockConfiguration.getPort();
        registry.add("services.auth.url", () -> "http://localhost:" + port);
        registry.add("services.user.url", () -> "http://localhost:" + port);
        registry.add("services.order.url", () -> "http://localhost:" + port);
        registry.add("wiremock.server.port", () -> port);
    }

    @Autowired
    protected WebTestClient webTestClient;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected WireMockServer wireMockServer;

    @BeforeEach
    void resetWireMock() {
        wireMockServer.resetAll();
    }
}