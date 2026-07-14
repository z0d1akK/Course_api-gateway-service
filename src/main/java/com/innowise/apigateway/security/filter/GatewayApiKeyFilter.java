package com.innowise.apigateway.security.filter;

import com.innowise.apigateway.common.constants.Headers;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GatewayApiKeyFilter extends AbstractGatewayFilterFactory<Object> {

    @Value("${gateway.api-key}")
    private String gatewayApiKey;

    @Override
    public GatewayFilter apply(Object config) {

        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest()
                    .mutate()
                    .header(Headers.GATEWAY_KEY, gatewayApiKey)
                    .build();

            return chain.filter(exchange.mutate()
                            .request(request)
                            .build()
            );
        };
    }
}
