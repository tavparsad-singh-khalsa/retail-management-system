package com.retail.sales_service.config;

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

    @Value("${billing-service.url}")
    private String billingServiceUrl;

    @Value("${billing-service.connect-timeout}")
    private Duration billingConnectTimeout;

    @Value("${billing-service.read-timeout}")
    private Duration billingReadTimeout;

    @Bean("productRestClient")
    public RestClient productRestClient(RestClient.Builder builder) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(readTimeout);

        return builder
                .baseUrl(productServiceUrl)
                .requestFactory(factory)
                .requestInterceptor(authInterceptor)
                .build();
    }

    @Bean("billingRestClient")
    public RestClient billingRestClient(RestClient.Builder builder) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(billingConnectTimeout)
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(billingReadTimeout);

        return builder
                .baseUrl(billingServiceUrl)
                .requestFactory(factory)
                .requestInterceptor(authInterceptor)
                .build();
    }
}
