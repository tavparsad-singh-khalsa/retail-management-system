package com.retail.sales_service.client.impl;

import com.retail.sales_service.client.ProductClient;
import com.retail.sales_service.dto.integration.request.InventoryAdjustRequest;
import com.retail.sales_service.dto.integration.response.InventoryOperationResponse;
import com.retail.sales_service.dto.integration.response.ProductVariantResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestProductClient implements ProductClient {

    private final RestClient productRestClient;

    private static final String VARIANT_BY_ID = "/api/v1/product-variants/%d";
    private static final String DEDUCT_INVENTORY = "/api/v1/inventory/sale-deduct";

    @Override
    public ProductVariantResponse getVariantById(Long variantId) {
        log.info("Fetching product variant from Product Service: id={}...", variantId);
        ProductVariantResponse response = productRestClient.get()
                .uri(String.format(VARIANT_BY_ID, variantId))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    log.error("Product Service returned error fetching variant {}. Status: {}", variantId, res.getStatusCode());
                    throw new com.retail.sales_service.exception.ProductServiceException("Failed to fetch product variant from Product Service. Status: " + res.getStatusCode());
                })
                .body(ProductVariantResponse.class);
        if (response == null) {
            throw new com.retail.sales_service.exception.ProductServiceException("Empty response from Product Service for variant id: " + variantId);
        }
        return response;
    }

    @Override
    public InventoryOperationResponse deductInventory(InventoryAdjustRequest request) {
        log.info("Requesting inventory deduction from Product Service: items={} ref={}", request.getItems() == null ? 0 : request.getItems().size(), request.getReferenceNumber());
        InventoryOperationResponse response = productRestClient.post()
                .uri(DEDUCT_INVENTORY)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    log.error("Product Service returned error during inventory deduction. Status: {}", res.getStatusCode());
                    throw new com.retail.sales_service.exception.ProductServiceException("Failed to deduct inventory in Product Service. Status: " + res.getStatusCode());
                })
                .body(InventoryOperationResponse.class);
        if (response == null) {
            throw new com.retail.sales_service.exception.ProductServiceException("Empty response from Product Service during inventory deduction");
        }
        return response;
    }
}
