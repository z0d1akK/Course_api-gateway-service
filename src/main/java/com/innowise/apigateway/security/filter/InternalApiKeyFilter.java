package com.innowise.apigateway.security.filter;

import com.innowise.apigateway.common.constants.Headers;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.http.server.reactive.ServerHttpRequest;

@Component
@RequiredArgsConstructor
public class InternalApiKeyFilter extends AbstractGatewayFilterFactory<Object> {

    @Value("${internal.api-key}")
    private String internalApiKey;

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {

            ServerHttpRequest request = exchange.getRequest();

            if (request.getURI().getPath().startsWith("/internal/")) {
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header(Headers.INTERNAL_KEY, internalApiKey)
                        .build();
                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            }

            return chain.filter(exchange);
        };
    }
}