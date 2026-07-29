package com.retail.purchase_service.service.impl;

import com.retail.purchase_service.dto.request.CreateSupplierRequest;
import com.retail.purchase_service.dto.request.UpdateSupplierRequest;
import com.retail.purchase_service.dto.response.SupplierResponse;
import com.retail.purchase_service.entity.Supplier;
import com.retail.purchase_service.exception.SupplierAlreadyExistsException;
import com.retail.purchase_service.exception.SupplierNotFoundException;
import com.retail.purchase_service.repository.SupplierRepository;
import com.retail.purchase_service.service.interfaces.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;

    @Override
    @Transactional
    public SupplierResponse createSupplier(CreateSupplierRequest request) {

        // Normalize input
        String name = request.getName().trim();
        String contactPerson = safeTrim(request.getContactPerson());
        String phone = safeTrim(request.getPhone());
        String gstNumber = safeTrim(request.getGstNumber());
        String email = safeTrim(request.getEmail());
        String address = safeTrim(request.getAddress());

        // Business Rule:
        // Multiple suppliers may share the same email address.
        // Therefore, email uniqueness is not enforced.

        // 1. Validate Duplicate Name
        if (supplierRepository.existsByNameIgnoreCase(name)) {
            throw new SupplierAlreadyExistsException("Supplier with name '" + name + "' already exists.");
        }

        // 2. Validate Duplicate Phone (if provided)
        if (hasText(phone)) {
            if (supplierRepository.existsByPhone(phone)) {
                throw new SupplierAlreadyExistsException("Phone number is already registered to another supplier.");
            }
        }

        // 3. Validate Duplicate GST Number (if provided)
        if (hasText(gstNumber)) {
            if (supplierRepository.existsByGstNumber(gstNumber)) {
                throw new SupplierAlreadyExistsException("GST number is already registered to another supplier.");
            }
        }

        // 4. Create Entity
        Supplier supplier = Supplier.builder()
                .name(name)
                .contactPerson(contactPerson)
                .phone(phone)
                .gstNumber(gstNumber)
                .email(email)
                .address(address)
                .build();

        // 5 & 6. Save Entity
        Supplier savedSupplier = supplierRepository.save(supplier);

        // 7. Map to Response
        return mapToResponse(savedSupplier);
    }

    @Override
    @Transactional
    public SupplierResponse updateSupplier(Long id, UpdateSupplierRequest request) {

        // 1. Find supplier by ID
        Supplier existingSupplier = findSupplierById(id);

        // Normalize input
        String newName = request.getName().trim();
        String newContactPerson = safeTrim(request.getContactPerson());
        String newPhone = safeTrim(request.getPhone());
        String newGstNumber = safeTrim(request.getGstNumber());
        String newEmail = safeTrim(request.getEmail());
        String newAddress = safeTrim(request.getAddress());

        // 2. If name changed, check duplicate name
        if (!existingSupplier.getName().equalsIgnoreCase(newName)) {
            if (supplierRepository.existsByNameIgnoreCase(newName)) {
                throw new SupplierAlreadyExistsException("Supplier with name '" + newName + "' already exists.");
            }
        }

        // 3. If phone changed and provided, check duplicate phone
        if (hasText(newPhone) && !Objects.equals(newPhone, existingSupplier.getPhone())) {
            if (supplierRepository.existsByPhone(newPhone)) {
                throw new SupplierAlreadyExistsException("Phone number is already registered to another supplier.");
            }
        }

        // 4. If GST changed and provided, check duplicate GST
        if (hasText(newGstNumber) && !Objects.equals(newGstNumber, existingSupplier.getGstNumber())) {
            if (supplierRepository.existsByGstNumber(newGstNumber)) {
                throw new SupplierAlreadyExistsException("GST number is already registered to another supplier.");
            }
        }

        // 5. Update mutable fields
        existingSupplier.setName(newName);
        existingSupplier.setContactPerson(newContactPerson);
        existingSupplier.setPhone(newPhone);
        existingSupplier.setGstNumber(newGstNumber);
        existingSupplier.setEmail(newEmail);
        existingSupplier.setAddress(newAddress);

        // 6. Save
        Supplier updatedSupplier = supplierRepository.save(existingSupplier);

        // 7. Return SupplierResponse
        return mapToResponse(updatedSupplier);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(Long id) {
        Supplier supplier = findSupplierById(id);
        return mapToResponse(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllActiveSuppliers() {
        return supplierRepository.findByIsActiveTrue().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void activateSupplier(Long id) {
        Supplier supplier = findSupplierById(id);

        if (!supplier.getIsActive()) {
            supplier.setIsActive(true);
            supplierRepository.save(supplier);
        }
    }

    @Override
    @Transactional
    public void deactivateSupplier(Long id) {
        Supplier supplier = findSupplierById(id);

        if (supplier.getIsActive()) {
            supplier.setIsActive(false);
            supplierRepository.save(supplier);
        }
    }

    // --- Private Helper Methods ---

    private Supplier findSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException("Supplier not found with id: " + id));
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String safeTrim(String value) {
        if (value == null) {
            return null;
        }

        value = value.trim();
        return value.isEmpty() ? null : value;
    }

    private SupplierResponse mapToResponse(Supplier supplier) {
        return SupplierResponse.builder()
                .id(supplier.getId())
                .name(supplier.getName())
                .contactPerson(supplier.getContactPerson())
                .phone(supplier.getPhone())
                .gstNumber(supplier.getGstNumber())
                .email(supplier.getEmail())
                .address(supplier.getAddress())
                .isActive(supplier.getIsActive())
                .build();
    }
}