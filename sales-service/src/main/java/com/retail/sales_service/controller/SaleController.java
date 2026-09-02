package com.retail.sales_service.controller;

import com.retail.sales_service.dto.request.CreateSaleRequest;
import com.retail.sales_service.dto.request.PaymentRequest;
import com.retail.sales_service.dto.response.CreateSaleResult;
import com.retail.sales_service.dto.response.SaleResponse;
import com.retail.sales_service.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    public ResponseEntity<SaleResponse> createSale(
            @Valid @RequestBody CreateSaleRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        CreateSaleResult result = saleService.createSale(request, idempotencyKey);
        HttpStatus status = result.replayed() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(result.response());
    }

    @GetMapping("/{saleId}")
    public SaleResponse getSaleById(@PathVariable Long saleId) {
        return saleService.getSaleById(saleId);
    }

    @GetMapping
    public Page<SaleResponse> getAllSales(@PageableDefault(size = 20) Pageable pageable) {
        return saleService.getAllSales(pageable);
    }

    @PatchMapping("/{saleId}/cancel")
    public SaleResponse cancelSale(@PathVariable Long saleId) {
        return saleService.cancelSale(saleId);
    }

    @PostMapping("/{saleId}/payments")
    public SaleResponse settlePayment(@PathVariable Long saleId,
                                      @Valid @RequestBody PaymentRequest request) {
        return saleService.settlePayment(saleId, request);
    }
}
