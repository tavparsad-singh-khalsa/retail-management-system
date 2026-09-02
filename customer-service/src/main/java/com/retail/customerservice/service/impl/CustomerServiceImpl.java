package com.retail.customerservice.service.impl;

import com.retail.customerservice.dto.request.CreateCustomerAddressRequest;
import com.retail.customerservice.dto.request.CreateCustomerRequest;
import com.retail.customerservice.dto.request.UpdateCustomerAddressRequest;
import com.retail.customerservice.dto.request.UpdateCustomerRequest;
import com.retail.customerservice.dto.response.CustomerAddressResponse;
import com.retail.customerservice.dto.response.CustomerResponse;
import com.retail.customerservice.entity.Customer;
import com.retail.customerservice.entity.CustomerAddress;
import com.retail.customerservice.enums.CustomerStatus;
import com.retail.customerservice.exception.CustomerAddressNotFoundException;
import com.retail.customerservice.exception.CustomerAlreadyExistsException;
import com.retail.customerservice.exception.CustomerNotFoundException;
import com.retail.customerservice.mapper.CustomerMapper;
import com.retail.customerservice.repository.CustomerRepository;
import com.retail.customerservice.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        String mobileNumber = normalizeInput(request.getMobileNumber());
        String email = normalizeInput(request.getEmail());

        validateUniqueMobileNumber(mobileNumber, null);
        validateUniqueEmail(email, null);

        Customer customer = customerMapper.toCustomer(request);
        // Persist normalized values (avoid storing leading/trailing spaces)
        customer.setMobileNumber(mobileNumber);
        customer.setEmail(email);
        customer.setCustomerCode(generateCustomerCode());
        customer.setCustomerStatus(CustomerStatus.ACTIVE);
        customer.setActive(true);

        Customer savedCustomer = customerRepository.save(customer);
        return customerMapper.toCustomerResponse(savedCustomer);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(Long customerId, UpdateCustomerRequest request) {
        Customer customer = findCustomerById(customerId);

        String mobileNumber = normalizeInput(request.getMobileNumber());
        String email = normalizeInput(request.getEmail());

        validateUniqueMobileNumber(mobileNumber, customerId);
        validateUniqueEmail(email, customerId);

        customerMapper.updateCustomer(request, customer);
        // Ensure normalized values are persisted
        customer.setMobileNumber(mobileNumber);
        customer.setEmail(email);

        Customer updatedCustomer = customerRepository.save(customer);
        return customerMapper.toCustomerResponse(updatedCustomer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long customerId) {
        return customerMapper.toCustomerResponse(findCustomerById(customerId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable).map(customerMapper::toCustomerResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByCode(String customerCode) {
        Customer customer = customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with code: " + customerCode));
        return customerMapper.toCustomerResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByMobile(String mobileNumber) {
        String normalizedMobile = normalizeInput(mobileNumber);
        Customer customer = customerRepository.findByMobileNumber(normalizedMobile)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with mobile number: " + normalizedMobile));
        return customerMapper.toCustomerResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> searchCustomers(String keyword) {
        if (!hasText(keyword)) {
            return getAllCustomers(Pageable.unpaged()).getContent();
        }

        String normalizedKeyword = keyword.trim().toLowerCase();
        List<Customer> customers = customerRepository.searchByKeyword(normalizedKeyword);
        return customers.stream()
                .map(customerMapper::toCustomerResponse)
                .toList();
    }

    @Override
    @Transactional
    public CustomerResponse activateCustomer(Long customerId) {
        Customer customer = findCustomerById(customerId);
        if (!Boolean.TRUE.equals(customer.getActive())) {
            customer.setActive(true);
            customer.setCustomerStatus(CustomerStatus.ACTIVE);
            customerRepository.save(customer);
        }
        return customerMapper.toCustomerResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse deactivateCustomer(Long customerId) {
        Customer customer = findCustomerById(customerId);
        if (Boolean.TRUE.equals(customer.getActive())) {
            customer.setActive(false);
            customer.setCustomerStatus(CustomerStatus.INACTIVE);
            customerRepository.save(customer);
        }
        return customerMapper.toCustomerResponse(customer);
    }

    @Override
    @Transactional
    public CustomerAddressResponse addAddress(Long customerId, CreateCustomerAddressRequest request) {
        Customer customer = findCustomerById(customerId);
        CustomerAddress address = customerMapper.toCustomerAddress(request, customer);

        applyDefaultAddressRule(customer, address);
        customer.addAddress(address);
        customerRepository.save(customer);
        return customerMapper.toCustomerAddressResponse(address);
    }

    @Override
    @Transactional
    public CustomerAddressResponse updateAddress(Long customerId, Long addressId, UpdateCustomerAddressRequest request) {
        Customer customer = findCustomerById(customerId);
        CustomerAddress address = findAddressByCustomer(customer, addressId);

        customerMapper.updateCustomerAddress(request, address);
        applyDefaultAddressRule(customer, address);
        customerRepository.save(customer);
        return customerMapper.toCustomerAddressResponse(address);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerAddressResponse> getCustomerAddresses(Long customerId) {
        Customer customer = findCustomerById(customerId);
        return customer.getAddresses().stream()
                .map(customerMapper::toCustomerAddressResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteAddress(Long customerId, Long addressId) {
        Customer customer = findCustomerById(customerId);
        CustomerAddress address = findAddressByCustomer(customer, addressId);
        customer.removeAddress(address);
        customerRepository.save(customer);
    }

    private Customer findCustomerById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));
    }

    private CustomerAddress findAddressByCustomer(Customer customer, Long addressId) {
        return customer.getAddresses().stream()
                .filter(address -> Objects.equals(address.getId(), addressId))
                .findFirst()
                .orElseThrow(() -> new CustomerAddressNotFoundException("Address not found with id: " + addressId));
    }

    private void validateUniqueMobileNumber(String mobileNumber, Long customerId) {
        if (!hasText(mobileNumber)) {
            return;
        }

        if (customerId == null) {
            if (customerRepository.existsByMobileNumber(mobileNumber)) {
                throw new CustomerAlreadyExistsException("Customer with mobile number '" + mobileNumber + "' already exists.");
            }
        } else {
            if (customerRepository.existsByMobileNumberAndIdNot(mobileNumber, customerId)) {
                throw new CustomerAlreadyExistsException("Customer with mobile number '" + mobileNumber + "' already exists.");
            }
        }
    }

    private void validateUniqueEmail(String email, Long customerId) {
        if (!hasText(email)) {
            return;
        }

        if (customerId == null) {
            if (customerRepository.existsByEmail(email)) {
                throw new CustomerAlreadyExistsException("Customer with email '" + email + "' already exists.");
            }
        } else {
            if (customerRepository.existsByEmailAndIdNot(email, customerId)) {
                throw new CustomerAlreadyExistsException("Customer with email '" + email + "' already exists.");
            }
        }
    }

    private String generateCustomerCode() {
        int year = LocalDate.now().getYear();
        long sequence = customerRepository.count() + 1;

        while (true) {
            String candidate = "CUS-" + year + "-" + String.format("%06d", sequence);
            if (!customerRepository.existsByCustomerCode(candidate)) {
                return candidate;
            }
            sequence++;
        }
    }

    private String normalizeInput(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private void applyDefaultAddressRule(Customer customer, CustomerAddress address) {
        if (Boolean.TRUE.equals(address.getDefaultAddress())) {
            customer.getAddresses().forEach(existingAddress -> {
                if (!Objects.equals(existingAddress.getId(), address.getId()) && Boolean.TRUE.equals(existingAddress.getDefaultAddress())) {
                    existingAddress.setDefaultAddress(false);
                }
            });
            return;
        }

        if (customer.getAddresses().stream().noneMatch(existingAddress -> Boolean.TRUE.equals(existingAddress.getDefaultAddress()))) {
            address.setDefaultAddress(true);
        }
    }

}
