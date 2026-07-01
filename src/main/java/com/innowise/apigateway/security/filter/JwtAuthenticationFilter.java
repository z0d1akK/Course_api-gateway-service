package com.innowise.apigateway.security.filter;

import com.innowise.apigateway.client.auth.AuthServiceClient;
import com.innowise.apigateway.client.auth.dto.request.ValidateTokenRequestDto;
import com.innowise.apigateway.client.auth.dto.response.TokenValidationResponseDto;
import com.innowise.apigateway.common.constants.Headers;
import com.innowise.apigateway.common.constants.SecurityConstants;
import com.innowise.apigateway.common.constants.messages.ErrorMessages;
import com.innowise.apigateway.common.constants.paths.ApiPaths;
import com.innowise.apigateway.common.constants.paths.SecurityPaths;
import com.innowise.apigateway.common.exception.ServiceCommunicationException;
import com.innowise.apigateway.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final AuthServiceClient authServiceClient;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        if (isPublicPath(exchange.getRequest()) || isInternalPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null) {
            return Mono.error(new UnauthorizedException(ErrorMessages.AUTHORIZATION_HEADER_MISSING));
        }

        if (!authHeader.startsWith(SecurityConstants.BEARER_PREFIX)) {
            return Mono.error(new UnauthorizedException(ErrorMessages.AUTHORIZATION_HEADER_INVALID));
        }

        String token = authHeader.substring(SecurityConstants.BEARER_PREFIX.length());

        return authServiceClient
                .validateToken(ValidateTokenRequestDto.builder()
                        .token(token)
                        .build()
                )
                .flatMap(validation ->
                        chain.filter(exchange.mutate()
                                .request(addHeaders(exchange.getRequest(), validation))
                                .build()
                        )
                )
                .onErrorMap(error -> {
                    if (error instanceof ServiceCommunicationException) {
                        return new UnauthorizedException(ErrorMessages.INVALID_TOKEN);
                    }
                    return error;
                });
    }

    private ServerHttpRequest addHeaders(ServerHttpRequest request, TokenValidationResponseDto validation) {
        return request.mutate()
                .header(Headers.USER_ID, validation.getUserId().toString())
                .header(Headers.USER_ROLE, validation.getRole().name())
                .header(Headers.USER_LOGIN, validation.getLogin())
                .build();
    }

    private boolean isPublicPath(ServerHttpRequest request) {

        String path = request.getURI().getPath();

        return path.equals(ApiPaths.LOGIN)
                || path.equals(ApiPaths.REFRESH)
                || path.equals(ApiPaths.REGISTER)
                || (request.getMethod() == HttpMethod.GET && path.equals(ApiPaths.ITEMS))
                || (request.getMethod() == HttpMethod.GET && path.startsWith(ApiPaths.ITEMS_BY_ID))
                || path.startsWith(SecurityPaths.SWAGGER)
                || path.startsWith(SecurityPaths.API_DOCS)
                || path.startsWith(SecurityPaths.AUTH_DOCS_PREFIX)
                || path.startsWith(SecurityPaths.USER_DOCS_PREFIX)
                || path.startsWith(SecurityPaths.ORDER_DOCS_PREFIX)
                || path.startsWith(SecurityPaths.PAYMENT_DOCS_PREFIX);
    }

    private boolean isInternalPath(String path) {
        return path.startsWith(SecurityPaths.INTERNAL);
    }

    @Override
    public int getOrder() {
        return -100;
    }
}