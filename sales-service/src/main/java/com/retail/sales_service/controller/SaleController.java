package com.retail.sales_service.controller;

import com.retail.sales_service.dto.request.CreateSaleRequest;
import com.retail.sales_service.dto.response.SaleResponse;
import com.retail.sales_service.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaleResponse createSale(@Valid @RequestBody CreateSaleRequest request) {
        return saleService.createSale(request);
    }

    @GetMapping("/{saleId}")
    public SaleResponse getSaleById(@PathVariable Long saleId) {
        return saleService.getSaleById(saleId);
    }

    @GetMapping
    public List<SaleResponse> getAllSales() {
        return saleService.getAllSales();
    }

    @PatchMapping("/{saleId}/cancel")
    public SaleResponse cancelSale(@PathVariable Long saleId) {
        return saleService.cancelSale(saleId);
    }
}
