package com.innowise.apigateway.security.filter;

import com.innowise.apigateway.common.constants.Headers;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ForwardedHeadersFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        ServerHttpRequest mutatedRequest = request.mutate()
                .header(Headers.FORWARDED_HOST, request.getURI().getHost())
                .header(Headers.FORWARDED_PORT, String.valueOf(request.getURI().getPort()))
                .header(Headers.FORWARDED_PROTO, request.getURI().getScheme())
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -1;
    }
}