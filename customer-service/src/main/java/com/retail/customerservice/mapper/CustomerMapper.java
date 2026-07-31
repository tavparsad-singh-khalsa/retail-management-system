package com.retail.customerservice.mapper;

import com.retail.customerservice.dto.request.CreateCustomerAddressRequest;
import com.retail.customerservice.dto.request.CreateCustomerRequest;
import com.retail.customerservice.dto.request.UpdateCustomerAddressRequest;
import com.retail.customerservice.dto.request.UpdateCustomerRequest;
import com.retail.customerservice.dto.response.CustomerAddressResponse;
import com.retail.customerservice.dto.response.CustomerResponse;
import com.retail.customerservice.entity.Customer;
import com.retail.customerservice.entity.CustomerAddress;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomerMapper {

    public Customer toCustomer(CreateCustomerRequest request) {
        return Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .mobileNumber(request.getMobileNumber())
                .email(request.getEmail())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .anniversary(request.getAnniversary())
                .build();
    }

    public void updateCustomer(UpdateCustomerRequest request, Customer customer) {
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setMobileNumber(request.getMobileNumber());
        customer.setEmail(request.getEmail());
        customer.setGender(request.getGender());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setAnniversary(request.getAnniversary());
        customer.setCustomerStatus(request.getCustomerStatus());
        customer.setActive(request.getActive());
    }

    public CustomerResponse toCustomerResponse(Customer customer) {
        List<CustomerAddressResponse> addressResponses = customer.getAddresses() == null
                ? List.of()
                : customer.getAddresses().stream()
                .map(this::toCustomerAddressResponse)
                .toList();

        return CustomerResponse.builder()
                .id(customer.getId())
                .customerCode(customer.getCustomerCode())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .mobileNumber(customer.getMobileNumber())
                .email(customer.getEmail())
                .gender(customer.getGender())
                .dateOfBirth(customer.getDateOfBirth())
                .anniversary(customer.getAnniversary())
                .customerStatus(customer.getCustomerStatus())
                .active(customer.getActive())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .addresses(addressResponses)
                .build();
    }

    public CustomerAddress toCustomerAddress(CreateCustomerAddressRequest request, Customer customer) {
        CustomerAddress address = CustomerAddress.builder()
                .customer(customer)
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .addressType(request.getAddressType())
                .defaultAddress(request.getDefaultAddress())
                .active(request.getActive())
                .build();
        return address;
    }

    public void updateCustomerAddress(UpdateCustomerAddressRequest request, CustomerAddress address) {
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setPostalCode(request.getPostalCode());
        address.setAddressType(request.getAddressType());
        address.setDefaultAddress(request.getDefaultAddress());
        address.setActive(request.getActive());
    }

    public CustomerAddressResponse toCustomerAddressResponse(CustomerAddress address) {
        return CustomerAddressResponse.builder()
                .id(address.getId())
                .customerId(address.getCustomer() == null ? null : address.getCustomer().getId())
                .customerCode(address.getCustomer() == null ? null : address.getCustomer().getCustomerCode())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .postalCode(address.getPostalCode())
                .addressType(address.getAddressType())
                .defaultAddress(address.getDefaultAddress())
                .active(address.getActive())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }
}
