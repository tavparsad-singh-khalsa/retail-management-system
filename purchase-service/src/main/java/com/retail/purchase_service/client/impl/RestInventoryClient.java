package com.retail.purchase_service.client.impl;

import com.retail.purchase_service.client.InventoryClient;
import com.retail.purchase_service.dto.integration.request.InventoryReceiveRequest;
import com.retail.purchase_service.dto.integration.response.InventoryOperationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestInventoryClient implements InventoryClient {

    private final RestClient productRestClient;

    private static final String RECEIVE_INVENTORY_URI = "/api/v1/inventory/purchase-receive";

    @Override
    public InventoryOperationResponse receiveInventory(InventoryReceiveRequest request) {

        log.info("Calling Product Service to receive inventory... Reference: {}, Items: {}",
                request.getReferenceNumber(), request.getItems().size());

        InventoryOperationResponse response = productRestClient.post()
                .uri(RECEIVE_INVENTORY_URI)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    log.error("Product Service returned error. Status code: {}", res.getStatusCode());
                    throw new RuntimeException("Failed to update inventory in Product Service. Status: " + res.getStatusCode());
                })
                .body(InventoryOperationResponse.class);

        if (response != null) {
            log.info("Inventory updated successfully. Reference: {}, Transaction ID: {}",
                    response.getReferenceNumber(), response.getTransactionId());
        }

        return response;
    }
}