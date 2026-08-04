package com.retail.billingservice.repository;

import com.retail.billingservice.entity.Invoice;
import com.retail.billingservice.model.InvoiceStatus;
import com.retail.billingservice.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findBySaleId(Long saleId);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    @Query(value = "SELECT nextval('invoice_number_seq')", nativeQuery = true)
    long nextInvoiceNumberSequence();

    List<Invoice> findByCustomerId(Long customerId);

    List<Invoice> findByPaymentStatus(PaymentStatus paymentStatus);

    List<Invoice> findByInvoiceStatus(InvoiceStatus invoiceStatus);

}
