package com.innowise.apigateway.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${internal.api-key}")
    private String internalApiKey;

    @Value("${services.auth.url}")
    private String authUrl;

    @Value("${services.user.url}")
    private String userUrl;

    @Value("${services.order.url}")
    private String orderUrl;

    @Bean
    @Qualifier("authWebClient")
    public WebClient authWebClient(WebClient.Builder builder) {
        return builder.baseUrl(authUrl).defaultHeader("X-Internal-Key", internalApiKey).build();
    }

    @Bean
    @Qualifier("userWebClient")
    public WebClient userWebClient(WebClient.Builder builder) {
        return builder.baseUrl(userUrl).defaultHeader("X-Internal-Key", internalApiKey).build();
    }

    @Bean
    @Qualifier("orderWebClient")
    public WebClient orderWebClient(WebClient.Builder builder) {
        return builder.baseUrl(orderUrl).defaultHeader("X-Internal-Key", internalApiKey).build();
    }
}