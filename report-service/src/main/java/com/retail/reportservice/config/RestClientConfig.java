package com.retail.reportservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    private final AuthorizationHeaderPropagationInterceptor authInterceptor;

    public RestClientConfig(AuthorizationHeaderPropagationInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Value("${app.services.sales-url:http://localhost:9094}")
    private String salesServiceUrl;

    @Value("${app.services.purchase-url:http://localhost:9093}")
    private String purchaseServiceUrl;

    @Value("${app.services.billing-url:http://localhost:9096}")
    private String billingServiceUrl;

    @Value("${app.services.product-url:http://localhost:9092}")
    private String productServiceUrl;

    @Value("${app.services.customer-url:http://localhost:9095}")
    private String customerServiceUrl;

    @Value("${app.services.notification-url:http://localhost:9097}")
    private String notificationServiceUrl;

    private SimpleClientHttpRequestFactory requestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(10).toMillis());
        return factory;
    }

    @Bean
    public RestClient salesRestClient() {
        return RestClient.builder()
                .baseUrl(salesServiceUrl)
                .requestFactory(requestFactory()).requestInterceptor(authInterceptor).build();
    }

    @Bean
    public RestClient purchaseRestClient() {
        return RestClient.builder()
                .baseUrl(purchaseServiceUrl)
                .requestFactory(requestFactory()).requestInterceptor(authInterceptor).build();
    }

    @Bean
    public RestClient billingRestClient() {
        return RestClient.builder()
                .baseUrl(billingServiceUrl)
                .requestFactory(requestFactory()).requestInterceptor(authInterceptor).build();
    }

    @Bean
    public RestClient productRestClient() {
        return RestClient.builder()
                .baseUrl(productServiceUrl)
                .requestFactory(requestFactory()).requestInterceptor(authInterceptor).build();
    }

    @Bean
    public RestClient customerRestClient() {
        return RestClient.builder()
                .baseUrl(customerServiceUrl)
                .requestFactory(requestFactory()).requestInterceptor(authInterceptor).build();
    }

    @Bean
    public RestClient notificationRestClient() {
        return RestClient.builder()
                .baseUrl(notificationServiceUrl)
                .requestFactory(requestFactory()).requestInterceptor(authInterceptor).build();
    }
}
