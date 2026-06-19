package com.innowise.apigateway.unit.security;

import com.innowise.apigateway.common.constants.Headers;
import com.innowise.apigateway.security.filter.InternalApiKeyFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalApiKeyFilterTest {

    @Mock
    private GatewayFilterChain chain;

    @InjectMocks
    private InternalApiKeyFilter filterFactory;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                filterFactory,
                "internalApiKey",
                "test-key"
        );
    }

    @Test
    @DisplayName("Should add internal API key header")
    void shouldAddInternalApiKeyHeader() {

        MockServerHttpRequest request = MockServerHttpRequest.post("/internal/users").build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilter gatewayFilter = filterFactory.apply(new Object());

        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(gatewayFilter.filter(exchange, chain)).verifyComplete();

        verify(chain).filter(argThat(ex ->
                "test-key".equals(ex.getRequest()
                        .getHeaders()
                        .getFirst(Headers.INTERNAL_KEY)
                )
        ));
    }
}