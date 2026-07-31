package com.retail.customerservice.repository;

import com.retail.customerservice.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByCustomerCode(String customerCode);

    boolean existsByMobileNumber(String mobileNumber);

    boolean existsByEmail(String email);

    boolean existsByMobileNumberAndIdNot(String mobileNumber, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<Customer> findByCustomerCode(String customerCode);

    Optional<Customer> findByMobileNumber(String mobileNumber);

    Optional<Customer> findByEmail(String email);

    @Query("SELECT c FROM Customer c WHERE " +
            "LOWER(c.customerCode) LIKE %:kw% OR LOWER(c.firstName) LIKE %:kw% OR LOWER(c.lastName) LIKE %:kw% OR LOWER(c.mobileNumber) LIKE %:kw% OR LOWER(c.email) LIKE %:kw%")
    List<Customer> searchByKeyword(@Param("kw") String keyword);

    List<Customer> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName,
                                                                                     String lastName);

    List<Customer> findByActiveTrue();
}
