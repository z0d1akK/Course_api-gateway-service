package com.innowise.apigateway.filter;

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
                        .header("X-Internal-Key", internalApiKey)
                        .build();
                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            }

            return chain.filter(exchange);
        };
    }
}