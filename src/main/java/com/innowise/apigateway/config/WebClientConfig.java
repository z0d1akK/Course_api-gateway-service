package com.innowise.apigateway.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @Qualifier("authWebClient")
    public WebClient authWebClient(WebClient.Builder builder, @Value("${services.auth.url}") String authUrl) {
        return builder.baseUrl(authUrl).build();
    }

    @Bean
    @Qualifier("userWebClient")
    public WebClient userWebClient(WebClient.Builder builder, @Value("${services.user.url}") String userUrl) {
        return builder.baseUrl(userUrl).build();
    }

    @Bean
    @Qualifier("orderWebClient")
    public WebClient orderWebClient(WebClient.Builder builder, @Value("${services.order.url}") String orderUrl) {
        return builder.baseUrl(orderUrl).build();
    }
}