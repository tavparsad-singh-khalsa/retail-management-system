package com.retail.sales_service.client.impl;

import com.retail.sales_service.client.ProductClient;
import com.retail.sales_service.dto.integration.request.InventoryAdjustRequest;
import com.retail.sales_service.dto.integration.request.StockMovementRequest;
import com.retail.sales_service.dto.integration.response.InventoryOperationResponse;
import com.retail.sales_service.dto.integration.response.InventoryResponse;
import com.retail.sales_service.dto.integration.response.ProductVariantResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class RestProductClient implements ProductClient {

    private final RestClient productRestClient;

    public RestProductClient(@Qualifier("productRestClient") RestClient productRestClient) {
        this.productRestClient = productRestClient;
    }

    private static final String VARIANT_BY_ID = "/api/v1/product-variants/%d";
    private static final String INVENTORY_BY_VARIANT = "/api/v1/inventory/variant/%d";
    private static final String DEDUCT_INVENTORY = "/api/v1/inventory/sale-deduct";
    private static final String STOCK_MOVEMENT = "/api/v1/stock-movements";

    @Override
    public InventoryResponse getInventoryByVariantId(Long variantId) {
        log.info("Fetching inventory from Product Service for variant ID: {}", variantId);
        InventoryResponse response = productRestClient.get()
                .uri(String.format(INVENTORY_BY_VARIANT, variantId))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    log.error("Product Service returned error fetching inventory for variant {}. Status: {}", variantId, res.getStatusCode());
                    throw new com.retail.sales_service.exception.ProductServiceException("Failed to fetch inventory from Product Service. Status: " + res.getStatusCode());
                })
                .body(InventoryResponse.class);
        if (response == null) {
            throw new com.retail.sales_service.exception.ProductServiceException("Empty response from Product Service for inventory of variant: " + variantId);
        }
        return response;
    }

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

    @Override
    public InventoryOperationResponse createStockMovement(StockMovementRequest request) {
        log.info("Creating stock movement in Product Service: type={} qty={} ref={}", request.getMovementType(), request.getQuantity(), request.getReferenceNumber());
        InventoryOperationResponse response = productRestClient.post()
                .uri(STOCK_MOVEMENT)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    log.error("Product Service returned error creating stock movement. Status: {}", res.getStatusCode());
                    throw new com.retail.sales_service.exception.ProductServiceException("Failed to create stock movement in Product Service. Status: " + res.getStatusCode());
                })
                .body(InventoryOperationResponse.class);
        if (response == null) {
            throw new com.retail.sales_service.exception.ProductServiceException("Empty response from Product Service creating stock movement");
        }
        return response;
    }
}
