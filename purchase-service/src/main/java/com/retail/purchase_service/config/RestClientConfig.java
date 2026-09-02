package com.retail.purchase_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class RestClientConfig {

    private final AuthorizationHeaderPropagationInterceptor authInterceptor;

    public RestClientConfig(AuthorizationHeaderPropagationInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Value("${product-service.url}")
    private String productServiceUrl;

    @Value("${product-service.connect-timeout}")
    private Duration connectTimeout;

    @Value("${product-service.read-timeout}")
    private Duration readTimeout;

    @Bean
    public RestClient productRestClient(RestClient.Builder builder) {
        // 1. Build the modern JDK HttpClient with a connection timeout
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();

        // 2. Wrap it in Spring's factory and set the read timeout
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(readTimeout);

        return builder
                .baseUrl(productServiceUrl)
                .requestFactory(factory)
                .requestInterceptor(authInterceptor)
                .build();
    }
}