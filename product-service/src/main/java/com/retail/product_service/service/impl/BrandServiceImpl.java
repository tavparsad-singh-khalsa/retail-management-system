package com.retail.product_service.service.impl;

import com.retail.product_service.dto.request.CreateBrandRequest;
import com.retail.product_service.dto.request.UpdateBrandRequest;
import com.retail.product_service.dto.response.BrandResponse;
import com.retail.product_service.entity.Brand;
import com.retail.product_service.exception.BrandAlreadyExistsException;
import com.retail.product_service.exception.BrandNotFoundException;
import com.retail.product_service.repository.BrandRepository;
import com.retail.product_service.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;

    @Transactional
    @Override
    public BrandResponse createBrand(CreateBrandRequest request) {
        // 1. Duplicate Name Check
        if (brandRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BrandAlreadyExistsException("Brand name already exists: " + request.getName());
        }

        // 2. Create Entity
        Brand brand = Brand.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        // 3. Save & Map
        Brand savedBrand = brandRepository.save(brand);
        return mapToResponse(savedBrand);
    }

    @Transactional
    @Override
    public BrandResponse updateBrand(Long id, UpdateBrandRequest request) {
        // 1. Find Brand
        Brand brand = getBrand(id);

        // 2. Name Changed? -> Duplicate Check
        if (request.getName() != null && !request.getName().equalsIgnoreCase(brand.getName())) {
            if (brandRepository.existsByNameIgnoreCase(request.getName())) {
                throw new BrandAlreadyExistsException("Brand name already exists: " + request.getName());
            }
            brand.setName(request.getName());
        }

        // 3. Update Other Fields (excluding isActive)
        if (request.getDescription() != null) {
            brand.setDescription(request.getDescription());
        }

        // 4. Save & Map
        Brand updatedBrand = brandRepository.save(brand);
        return mapToResponse(updatedBrand);
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponse getBrandById(Long id) {
        return mapToResponse(getBrand(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponse> getAllBrands() {
        return brandRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    @Override
    public void activateBrand(Long id) {
        Brand brand = getBrand(id);

        // Idempotency check
        if (brand.getIsActive()) {
            return;
        }

        brand.setIsActive(true);
        brandRepository.save(brand);
    }

    @Transactional
    @Override
    public void deactivateBrand(Long id) {
        Brand brand = getBrand(id);

        // Idempotency check
        if (!brand.getIsActive()) {
            return;
        }

        // Rule 5: No hierarchy check needed. Existing products remain intact.
        brand.setIsActive(false);
        brandRepository.save(brand);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponse> getAllActiveBrands() {
        return brandRepository.findByIsActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private Brand getBrand(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new BrandNotFoundException("Brand not found with ID: " + id));
    }

    private BrandResponse mapToResponse(Brand brand) {
        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .description(brand.getDescription())
                .isActive(brand.getIsActive())
                .createdAt(brand.getCreatedAt())
                .build();
    }
}
