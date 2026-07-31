package com.retail.customerservice.controller;

import com.retail.customerservice.dto.request.CreateCustomerAddressRequest;
import com.retail.customerservice.dto.request.UpdateCustomerAddressRequest;
import com.retail.customerservice.dto.response.CustomerAddressResponse;
import com.retail.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/customers/{customerId}/addresses")
@RequiredArgsConstructor
public class CustomerAddressController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerAddressResponse> addAddress(@PathVariable("customerId") Long customerId,
                                                              @Valid @RequestBody CreateCustomerAddressRequest request) {
        CustomerAddressResponse response = customerService.addAddress(customerId, request);
        return ResponseEntity.created(URI.create("/api/customers/" + customerId + "/addresses/" + response.getId())).body(response);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<CustomerAddressResponse> updateAddress(@PathVariable("customerId") Long customerId,
                                                                  @PathVariable("addressId") Long addressId,
                                                                  @Valid @RequestBody UpdateCustomerAddressRequest request) {
        CustomerAddressResponse response = customerService.updateAddress(customerId, addressId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CustomerAddressResponse>> getAddresses(@PathVariable("customerId") Long customerId) {
        return ResponseEntity.ok(customerService.getCustomerAddresses(customerId));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable("customerId") Long customerId,
                                              @PathVariable("addressId") Long addressId) {
        customerService.deleteAddress(customerId, addressId);
        return ResponseEntity.noContent().build();
    }
}
