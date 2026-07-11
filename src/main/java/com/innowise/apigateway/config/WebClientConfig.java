package com.innowise.apigateway.config;

import com.innowise.apigateway.common.constants.Headers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${internal.api-key}")
    private String internalApiKey;

    @Value("${gateway.api-key}")
    private String gatewayApiKey;

    @Value("${services.auth.url}")
    private String authUrl;

    @Value("${services.user.url}")
    private String userUrl;

    @Bean
    @Qualifier("authWebClient")
    public WebClient authWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(authUrl)
                .filter(addAuthHeaders())
                .build();
    }

    @Bean
    @Qualifier("userWebClient")
    public WebClient userWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(userUrl)
                .defaultHeader(Headers.INTERNAL_KEY, internalApiKey)
                .build();
    }

    private ExchangeFilterFunction addAuthHeaders() {
        return (request, next) -> {
            String path = request.url().getPath();

            ClientRequest modifiedRequest;
            if (path.startsWith("/internal/")) {
                modifiedRequest = ClientRequest.from(request)
                        .header(Headers.INTERNAL_KEY, internalApiKey)
                        .build();
            } else {
                modifiedRequest = ClientRequest.from(request)
                        .header(Headers.GATEWAY_KEY, gatewayApiKey)
                        .build();
            }
            return next.exchange(modifiedRequest);
        };
    }
}