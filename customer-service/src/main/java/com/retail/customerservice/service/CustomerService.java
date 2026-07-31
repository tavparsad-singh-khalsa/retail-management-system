package com.retail.customerservice.service;

import com.retail.customerservice.dto.request.CreateCustomerAddressRequest;
import com.retail.customerservice.dto.request.CreateCustomerRequest;
import com.retail.customerservice.dto.request.UpdateCustomerAddressRequest;
import com.retail.customerservice.dto.request.UpdateCustomerRequest;
import com.retail.customerservice.dto.response.CustomerAddressResponse;
import com.retail.customerservice.dto.response.CustomerResponse;

import java.util.List;

public interface CustomerService {

    CustomerResponse createCustomer(CreateCustomerRequest request);

    CustomerResponse updateCustomer(Long customerId, UpdateCustomerRequest request);

    CustomerResponse getCustomerById(Long customerId);

    List<CustomerResponse> getAllCustomers();

    CustomerResponse getCustomerByCode(String customerCode);

    CustomerResponse getCustomerByMobile(String mobileNumber);

    List<CustomerResponse> searchCustomers(String keyword);

    CustomerResponse activateCustomer(Long customerId);

    CustomerResponse deactivateCustomer(Long customerId);

    CustomerAddressResponse addAddress(Long customerId, CreateCustomerAddressRequest request);

    CustomerAddressResponse updateAddress(Long customerId, Long addressId, UpdateCustomerAddressRequest request);

    List<CustomerAddressResponse> getCustomerAddresses(Long customerId);

    void deleteAddress(Long customerId, Long addressId);
}
