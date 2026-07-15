package com.retail.product_service.service.impl;

import com.retail.product_service.dto.request.CreateVariantAttributeRequest;
import com.retail.product_service.dto.request.UpdateVariantAttributeRequest;
import com.retail.product_service.dto.response.VariantAttributeResponse;
import com.retail.product_service.entity.Attribute;
import com.retail.product_service.entity.AttributeValue;
import com.retail.product_service.entity.ProductVariant;
import com.retail.product_service.entity.VariantAttribute;
import com.retail.product_service.exception.*;
import com.retail.product_service.repository.AttributeRepository;
import com.retail.product_service.repository.AttributeValueRepository;
import com.retail.product_service.repository.ProductVariantRepository;
import com.retail.product_service.repository.VariantAttributeRepository;
import com.retail.product_service.service.VariantAttributeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VariantAttributeServiceImpl implements VariantAttributeService {

    private final VariantAttributeRepository variantAttributeRepository;
    private final ProductVariantRepository productVariantRepository;
    private final AttributeRepository attributeRepository;
    private final AttributeValueRepository attributeValueRepository;

    @Transactional
    @Override
    public VariantAttributeResponse createVariantAttribute(CreateVariantAttributeRequest request) {
        ProductVariant variant = getActiveVariantOrThrow(request.getProductVariantId());
        Attribute attribute = getActiveAttributeOrThrow(request.getAttributeId());
        AttributeValue attributeValue = getActiveAttributeValueOrThrow(request.getAttributeValueId());

        if (!attributeValue.getAttribute().getId().equals(attribute.getId())) {
            throw new InvalidAttributeValueException("The provided Attribute Value does not belong to the specified Attribute.");
        }

        if (variantAttributeRepository.existsByProductVariantAndAttribute(variant, attribute)) {
            throw new VariantAttributeAlreadyExistsException("This variant already has a value assigned for this attribute.");
        }

        VariantAttribute variantAttribute = VariantAttribute.builder()
                .productVariant(variant)
                .attribute(attribute)
                .attributeValue(attributeValue)
                .build();

        return mapToResponse(variantAttributeRepository.save(variantAttribute));
    }

    @Transactional
    @Override
    public VariantAttributeResponse updateVariantAttribute(Long id, UpdateVariantAttributeRequest request) {
        VariantAttribute variantAttribute = getVariantAttributeOrThrow(id);

        if (request.getAttributeValueId() != null && !request.getAttributeValueId().equals(variantAttribute.getAttributeValue().getId())) {
            AttributeValue newValue = getActiveAttributeValueOrThrow(request.getAttributeValueId());

            if (!newValue.getAttribute().getId().equals(variantAttribute.getAttribute().getId())) {
                throw new InvalidAttributeValueException("The new value does not match the original attribute category.");
            }

            variantAttribute.setAttributeValue(newValue);
        }

        return mapToResponse(variantAttributeRepository.save(variantAttribute));
    }

    @Override
    @Transactional(readOnly = true)
    public VariantAttributeResponse getVariantAttributeById(Long id) {
        return mapToResponse(getVariantAttributeOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VariantAttributeResponse> getAttributesByVariant(Long productVariantId) {
        // ⭐ IMPROVEMENT: Refactored to reuse getActiveVariantOrThrow
        ProductVariant variant = getActiveVariantOrThrow(productVariantId);

        // ⭐ IMPROVEMENT: Only fetch active mappings to protect frontend dropdowns
        return variantAttributeRepository.findByProductVariantAndIsActiveTrue(variant).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    @Override
    public void activateVariantAttribute(Long id) {
        VariantAttribute variantAttribute = getVariantAttributeOrThrow(id);

        if (variantAttribute.getIsActive()) return;

        getActiveVariantOrThrow(variantAttribute.getProductVariant().getId());
        // ⭐ IMPROVEMENT: Added explicit check for the parent Attribute
        getActiveAttributeOrThrow(variantAttribute.getAttribute().getId());
        getActiveAttributeValueOrThrow(variantAttribute.getAttributeValue().getId());

        variantAttribute.setIsActive(true);
        variantAttributeRepository.save(variantAttribute);
    }

    @Transactional
    @Override
    public void deactivateVariantAttribute(Long id) {
        VariantAttribute variantAttribute = getVariantAttributeOrThrow(id);

        if (!variantAttribute.getIsActive()) return;

        variantAttribute.setIsActive(false);
        variantAttributeRepository.save(variantAttribute);
    }

    // ==========================================
    // PRIVATE HELPER METHODS
    // ==========================================

    private VariantAttribute getVariantAttributeOrThrow(Long id) {
        return variantAttributeRepository.findById(id)
                .orElseThrow(() -> new VariantAttributeNotFoundException("Variant Attribute configuration not found with ID: " + id));
    }

    // ⭐ IMPROVEMENT: Deep validation down to the Brand layer
    private ProductVariant getActiveVariantOrThrow(Long id) {
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new ProductVariantNotFoundException("Product Variant not found with ID: " + id));

        if (!variant.getIsActive()) {
            throw new ProductInactiveException("Cannot use an inactive variant.");
        }
        if (!variant.getProduct().getIsActive()) {
            throw new ProductInactiveException("Cannot use variant: Parent product is inactive.");
        }
        if (!variant.getProduct().getCategory().getIsActive()) {
            throw new CategoryInactiveException("Cannot use variant: Parent category is inactive.");
        }
        if (!variant.getProduct().getBrand().getIsActive()) {
            throw new BrandInactiveException("Cannot use variant: Parent brand is inactive.");
        }
        return variant;
    }

    private Attribute getActiveAttributeOrThrow(Long id) {
        Attribute attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new AttributeNotFoundException("Attribute not found with ID: " + id));
        if (!attribute.getIsActive()) {
            throw new AttributeInactiveException("Cannot use an inactive attribute.");
        }
        return attribute;
    }

    private AttributeValue getActiveAttributeValueOrThrow(Long id) {
        AttributeValue value = attributeValueRepository.findById(id)
                .orElseThrow(() -> new AttributeValueNotFoundException("Attribute Value not found with ID: " + id));
        if (!value.getIsActive()) {
            // Future improvement noted: Consider adding AttributeValueInactiveException
            throw new AttributeInactiveException("Cannot use an inactive attribute value.");
        }
        return value;
    }

    private VariantAttributeResponse mapToResponse(VariantAttribute va) {
        // DTO confirmed to have productName from the previous step
        return VariantAttributeResponse.builder()
                .id(va.getId())
                .productVariantId(va.getProductVariant().getId())
                .productName(va.getProductVariant().getProduct().getName())
                .sku(va.getProductVariant().getSku())
                .attributeId(va.getAttribute().getId())
                .attributeName(va.getAttribute().getName())
                .attributeValueId(va.getAttributeValue().getId())
                .attributeValue(va.getAttributeValue().getValue())
                .isActive(va.getIsActive())
                .build();
    }
}