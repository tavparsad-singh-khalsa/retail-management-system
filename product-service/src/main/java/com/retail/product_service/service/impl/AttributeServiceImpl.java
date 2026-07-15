package com.retail.product_service.service.impl;

import com.retail.product_service.dto.request.CreateAttributeRequest;
import com.retail.product_service.dto.request.UpdateAttributeRequest;
import com.retail.product_service.dto.response.AttributeResponse;
import com.retail.product_service.entity.Attribute;
import com.retail.product_service.exception.AttributeAlreadyExistsException;
import com.retail.product_service.exception.AttributeInUseException;
import com.retail.product_service.exception.AttributeNotFoundException;
import com.retail.product_service.repository.AttributeRepository;
import com.retail.product_service.repository.AttributeValueRepository;
import com.retail.product_service.service.AttributeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttributeServiceImpl implements AttributeService {

    private final AttributeRepository attributeRepository;
    private final AttributeValueRepository attributeValueRepository;

    @Transactional
    @Override
    public AttributeResponse createAttribute(CreateAttributeRequest request) {
        if (attributeRepository.existsByNameIgnoreCase(request.getName())) {
            throw new AttributeAlreadyExistsException("Attribute name already exists: " + request.getName());
        }

        Attribute attribute = Attribute.builder()
                .name(request.getName())
                .description(request.getDescription())
                .dataType(request.getDataType())
                .required(false)
                .build();

        Attribute savedAttribute = attributeRepository.save(attribute);
        return mapToResponse(savedAttribute);
    }

    @Transactional
    @Override
    public AttributeResponse updateAttribute(Long id, UpdateAttributeRequest request) {
        // ⭐ IMPROVEMENT 3: Renamed for clear intent
        Attribute attribute = findAttributeOrThrow(id);

        if (request.getName() != null && !request.getName().equals(attribute.getName())) {
            if (attributeRepository.existsByNameIgnoreCase(request.getName())) {
                throw new AttributeAlreadyExistsException("Attribute name already exists: " + request.getName());
            }
            attribute.setName(request.getName());
        }

        if (request.getDescription() != null) {
            attribute.setDescription(request.getDescription());
        }

        if (request.getDataType() != null) {
            attribute.setDataType(request.getDataType());
        }

        Attribute updatedAttribute = attributeRepository.save(attribute);
        return mapToResponse(updatedAttribute);
    }

    @Override
    @Transactional(readOnly = true)
    public AttributeResponse getAttributeById(Long id) {
        return mapToResponse(findAttributeOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttributeResponse> getAllAttributes() {
        return attributeRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    @Override
    public void activateAttribute(Long id) {
        Attribute attribute = findAttributeOrThrow(id);

        if (attribute.getIsActive()) return;

        attribute.setIsActive(true);
        attributeRepository.save(attribute);
    }

    @Transactional
    @Override
    public void deactivateAttribute(Long id) {
        Attribute attribute = findAttributeOrThrow(id);

        if (!attribute.getIsActive()) return;

        if (attributeValueRepository.existsByAttributeAndIsActiveTrue(attribute)) {
            throw new AttributeInUseException("Cannot deactivate attribute: It currently has active attribute values assigned.");
        }

        attribute.setIsActive(false);
        attributeRepository.save(attribute);
    }

    // ==========================================
    // PRIVATE HELPER METHODS
    // ==========================================

    // ⭐ IMPROVEMENT 3: Renamed to perfectly describe the action
    private Attribute findAttributeOrThrow(Long id) {
        return attributeRepository.findById(id)
                .orElseThrow(() -> new AttributeNotFoundException("Attribute not found with ID: " + id));
    }

    private AttributeResponse mapToResponse(Attribute attribute) {

        long numberOfValues =
                attribute.getAttributeValues() != null
                        ? attribute.getAttributeValues().size()
                        : 0;

        return AttributeResponse.builder()
                .id(attribute.getId())
                .name(attribute.getName())
                .description(attribute.getDescription())
                .dataType(attribute.getDataType())
                .numberOfValues(numberOfValues)
                .isActive(attribute.getIsActive())
                .createdAt(attribute.getCreatedAt())
                .build();
    }
}