package com.retail.customerservice.controller;

import com.retail.customerservice.dto.request.CreateCustomerRequest;
import com.retail.customerservice.dto.request.UpdateCustomerRequest;
import com.retail.customerservice.dto.response.CustomerResponse;
import com.retail.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.created(URI.create("/api/customers/" + response.getId())).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(@PathVariable("id") Long id,
                                                           @Valid @RequestBody UpdateCustomerRequest request) {
        CustomerResponse response = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<CustomerResponse>> getAllCustomers(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(customerService.getAllCustomers(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<CustomerResponse> getCustomerByCode(@PathVariable("code") String code) {
        return ResponseEntity.ok(customerService.getCustomerByCode(code));
    }

    @GetMapping("/mobile/{mobile}")
    public ResponseEntity<CustomerResponse> getCustomerByMobile(@PathVariable("mobile") String mobile) {
        return ResponseEntity.ok(customerService.getCustomerByMobile(mobile));
    }

    @GetMapping("/search")
    public ResponseEntity<List<CustomerResponse>> searchCustomers(@RequestParam(value = "keyword", required = false) String keyword) {
        return ResponseEntity.ok(customerService.searchCustomers(keyword));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<CustomerResponse> activateCustomer(@PathVariable("id") Long id) {
        return ResponseEntity.ok(customerService.activateCustomer(id));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<CustomerResponse> deactivateCustomer(@PathVariable("id") Long id) {
        return ResponseEntity.ok(customerService.deactivateCustomer(id));
    }
}
