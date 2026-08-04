package com.retail.billingservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${sales-service.url:http://localhost:9094}")
    private String salesServiceUrl;

    @Value("${sales-service.connect-timeout:5s}")
    private Duration salesConnectTimeout;

    @Value("${sales-service.read-timeout:10s}")
    private Duration salesReadTimeout;

    @Value("${customer-service.url:http://localhost:9095}")
    private String customerServiceUrl;

    @Value("${customer-service.connect-timeout:5s}")
    private Duration customerConnectTimeout;

    @Value("${customer-service.read-timeout:10s}")
    private Duration customerReadTimeout;

    @Bean
    public RestClient salesRestClient(RestClient.Builder builder) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(salesConnectTimeout)
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(salesReadTimeout);

        return builder
                .baseUrl(salesServiceUrl)
                .requestFactory(factory)
                .build();
    }

    @Bean
    public RestClient customerRestClient(RestClient.Builder builder) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(customerConnectTimeout)
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(customerReadTimeout);

        return builder
                .baseUrl(customerServiceUrl)
                .requestFactory(factory)
                .build();
    }
}
