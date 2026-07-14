package com.innowise.apigateway.unit.security;

import com.innowise.apigateway.client.auth.AuthServiceClient;
import com.innowise.apigateway.client.auth.dto.request.ValidateTokenRequestDto;
import com.innowise.apigateway.client.auth.dto.response.TokenValidationResponseDto;
import com.innowise.apigateway.common.constants.Headers;
import com.innowise.apigateway.common.constants.SecurityConstants;
import com.innowise.apigateway.common.constants.paths.ApiPaths;
import com.innowise.apigateway.common.enums.Role;
import com.innowise.apigateway.common.exception.ServiceCommunicationException;
import com.innowise.apigateway.common.exception.UnauthorizedException;
import com.innowise.apigateway.security.filter.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Objects;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private AuthServiceClient authServiceClient;

    @Mock
    private GatewayFilterChain chain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private static final UUID USER_ID = UUID.randomUUID();

    private static final String VALID_TOKEN = "valid.jwt.token";

    private static final String INVALID_TOKEN = "invalid.jwt.token";

    @Nested
    @DisplayName("Public paths - no authentication required")
    class PublicPaths {

        @Test
        @DisplayName("Should skip authentication for login path")
        void shouldSkipAuthForLoginPath() {
            var exchange = createExchange(ApiPaths.LOGIN, null);
            when(chain.filter(exchange)).thenReturn(Mono.empty());

            var result = filter.filter(exchange, chain);

            StepVerifier.create(result).verifyComplete();
            verify(authServiceClient, never()).validateToken(any());
        }

        @Test
        @DisplayName("Should skip authentication for register path")
        void shouldSkipAuthForRegisterPath() {
            var exchange = createExchange(ApiPaths.REGISTER, null);
            when(chain.filter(exchange)).thenReturn(Mono.empty());

            var result = filter.filter(exchange, chain);

            StepVerifier.create(result).verifyComplete();
            verify(authServiceClient, never()).validateToken(any());
        }

        @Test
        @DisplayName("Should skip authentication for refresh path")
        void shouldSkipAuthForRefreshPath() {
            var exchange = createExchange(ApiPaths.REFRESH, null);
            when(chain.filter(exchange)).thenReturn(Mono.empty());

            var result = filter.filter(exchange, chain);

            StepVerifier.create(result).verifyComplete();
            verify(authServiceClient, never()).validateToken(any());
        }

        @Test
        @DisplayName("Should skip authentication for GET items")
        void shouldSkipAuthForGetItems() {
            var request = MockServerHttpRequest.get(ApiPaths.ITEMS).build();
            var exchange = MockServerWebExchange.from(request);
            when(chain.filter(exchange)).thenReturn(Mono.empty());

            var result = filter.filter(exchange, chain);

            StepVerifier.create(result).verifyComplete();
            verify(authServiceClient, never()).validateToken(any());
        }

        @Test
        @DisplayName("Should skip authentication for Swagger UI")
        void shouldSkipAuthForSwagger() {
            var exchange = createExchange("/swagger-ui/index.html", null);
            when(chain.filter(exchange)).thenReturn(Mono.empty());

            var result = filter.filter(exchange, chain);

            StepVerifier.create(result).verifyComplete();
            verify(authServiceClient, never()).validateToken(any());
        }
    }

    @Nested
    @DisplayName("Authentication required")
    class AuthRequired {

        @Test
        @DisplayName("Should throw exception when Authorization header is missing")
        void shouldThrowWhenAuthHeaderMissing() {
            var exchange = createExchange("/api/orders", null);

            var result = filter.filter(exchange, chain);

            StepVerifier.create(result)
                    .expectError(UnauthorizedException.class)
                    .verify();
            verify(chain, never()).filter(any());
        }

        @Test
        @DisplayName("Should throw exception when Authorization header format is invalid")
        void shouldThrowWhenAuthHeaderInvalidFormat() {
            var exchange = createExchange("/api/orders", "InvalidFormat token");

            var result = filter.filter(exchange, chain);

            StepVerifier.create(result)
                    .expectError(UnauthorizedException.class)
                    .verify();
            verify(chain, never()).filter(any());
        }

        @Test
        @DisplayName("Should validate token and add headers on success")
        void shouldValidateTokenAndAddHeaders() {
            var exchange = createExchange("/api/orders", SecurityConstants.BEARER_PREFIX + VALID_TOKEN);
            var validationResponse = TokenValidationResponseDto.builder()
                    .valid(true)
                    .userId(USER_ID)
                    .role(Role.ROLE_USER)
                    .login("user@example.com")
                    .build();

            when(authServiceClient.validateToken(any(ValidateTokenRequestDto.class)))
                    .thenReturn(Mono.just(validationResponse));
            when(chain.filter(any())).thenReturn(Mono.empty());

            var result = filter.filter(exchange, chain);

            StepVerifier.create(result).verifyComplete();
            verify(authServiceClient).validateToken(argThat(req ->
                    req.getToken().equals(VALID_TOKEN)
            ));
            verify(chain).filter(argThat(ex ->
                    Objects.equals(ex.getRequest().getHeaders().getFirst(Headers.USER_ID), USER_ID.toString())
            ));
        }

        @Test
        @DisplayName("Should return error when token validation fails")
        void shouldReturnErrorWhenValidationFails() {
            var exchange = createExchange("/api/orders", SecurityConstants.BEARER_PREFIX + INVALID_TOKEN);

            when(authServiceClient.validateToken(any(ValidateTokenRequestDto.class)))
                    .thenReturn(Mono.error(new ServiceCommunicationException(
                            HttpStatus.UNAUTHORIZED, "Invalid token")));

            var result = filter.filter(exchange, chain);

            StepVerifier.create(result)
                    .expectError(UnauthorizedException.class)
                    .verify();
            verify(chain, never()).filter(any());
        }
    }

    @Nested
    @DisplayName("Internal paths")
    class InternalPaths {

        @Test
        @DisplayName("Should skip authentication for internal paths")
        void shouldSkipAuthForInternalPaths() {
            var exchange = createExchange("/internal/users", null);
            when(chain.filter(exchange)).thenReturn(Mono.empty());

            var result = filter.filter(exchange, chain);

            StepVerifier.create(result).verifyComplete();
            verify(authServiceClient, never()).validateToken(any());
        }
    }

    private MockServerWebExchange createExchange(String path, String authHeader) {
        var requestBuilder = MockServerHttpRequest.get(path);
        if (authHeader != null) {
            requestBuilder.header(HttpHeaders.AUTHORIZATION, authHeader);
        }
        return MockServerWebExchange.from(requestBuilder.build());
    }
}