package com.retail.customerservice.repository;

import com.retail.customerservice.entity.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {

    List<CustomerAddress> findByCustomerId(Long customerId);

    List<CustomerAddress> findByCustomerIdAndActiveTrue(Long customerId);

    Optional<CustomerAddress> findByCustomerIdAndDefaultAddressTrue(Long customerId);
}
