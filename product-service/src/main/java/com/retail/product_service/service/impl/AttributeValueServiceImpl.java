package com.retail.product_service.service.impl;

import com.retail.product_service.dto.request.CreateAttributeValueRequest;
import com.retail.product_service.dto.request.UpdateAttributeValueRequest;
import com.retail.product_service.dto.response.AttributeValueResponse;
import com.retail.product_service.entity.Attribute;
import com.retail.product_service.entity.AttributeValue;
import com.retail.product_service.enums.AttributeDataType;
import com.retail.product_service.exception.*;
import com.retail.product_service.repository.AttributeRepository;
import com.retail.product_service.repository.AttributeValueRepository;
import com.retail.product_service.service.AttributeValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttributeValueServiceImpl implements AttributeValueService {

    private final AttributeValueRepository attributeValueRepository;
    private final AttributeRepository attributeRepository;

    @Transactional
    @Override
    public AttributeValueResponse createAttributeValue(CreateAttributeValueRequest request) {
        Attribute attribute = findActiveAttributeOrThrow(request.getAttributeId());

        // ⭐ IMPROVEMENT 2: Trim the value before any validation or database logic
        String trimmedValue = request.getValue().trim();

        if (attributeValueRepository.existsByAttributeAndValueIgnoreCase(attribute, trimmedValue)) {
            throw new AttributeValueAlreadyExistsException("This value already exists for the specified attribute.");
        }

        // Validate using the clean, trimmed string
        validateValueByDataType(trimmedValue, attribute.getDataType());

        AttributeValue attributeValue = AttributeValue.builder()
                .attribute(attribute)
                .value(trimmedValue)
                .build();

        return mapToResponse(attributeValueRepository.save(attributeValue));
    }

    @Transactional
    @Override
    public AttributeValueResponse updateAttributeValue(Long id, UpdateAttributeValueRequest request) {
        AttributeValue attributeValue = findAttributeValueOrThrow(id);

        if (request.getValue() != null) {
            // ⭐ IMPROVEMENT 2: Trim the value for updates
            String trimmedValue = request.getValue().trim();

            if (!trimmedValue.equalsIgnoreCase(attributeValue.getValue())) {
                if (attributeValueRepository.existsByAttributeAndValueIgnoreCase(attributeValue.getAttribute(), trimmedValue)) {
                    throw new AttributeValueAlreadyExistsException("This value already exists for the attribute.");
                }

                validateValueByDataType(trimmedValue, attributeValue.getAttribute().getDataType());
                attributeValue.setValue(trimmedValue);
            }
        }

        // ⭐ IMPROVEMENT 8: Maintained standard save implementation
        return mapToResponse(attributeValueRepository.save(attributeValue));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttributeValueResponse> getAttributeValuesByAttributeId(Long attributeId) {
        return attributeValueRepository.findByAttributeId(attributeId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<AttributeValueResponse> getActiveAttributeValuesByAttributeId(Long attributeId) {
        Attribute attribute = findActiveAttributeOrThrow(attributeId);

        return attributeValueRepository.findByAttributeAndIsActiveTrue(attribute).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AttributeValueResponse getAttributeValueById(Long id) {
        return mapToResponse(findAttributeValueOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttributeValueResponse> getAllAttributeValues() {
        return attributeValueRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    @Override
    public void activateAttributeValue(Long id) {
        AttributeValue attributeValue = findAttributeValueOrThrow(id);

        if (attributeValue.getIsActive()) return;

        if (!attributeValue.getAttribute().getIsActive()) {
            throw new AttributeInactiveException("Cannot activate value: Parent attribute is currently inactive.");
        }

        attributeValue.setIsActive(true);
        attributeValueRepository.save(attributeValue);
    }

    @Transactional
    @Override
    public void deactivateAttributeValue(Long id) {
        AttributeValue attributeValue = findAttributeValueOrThrow(id);

        if (!attributeValue.getIsActive()) return;

        attributeValue.setIsActive(false);
        attributeValueRepository.save(attributeValue);
    }

    // ==========================================
    // PRIVATE HELPER METHODS
    // ==========================================

    private AttributeValue findAttributeValueOrThrow(Long id) {
        return attributeValueRepository.findById(id)
                .orElseThrow(() -> new AttributeValueNotFoundException("Attribute Value not found with ID: " + id));
    }

    private Attribute findActiveAttributeOrThrow(Long id) {
        Attribute attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new AttributeNotFoundException("Attribute not found with ID: " + id));

        if (!attribute.getIsActive()) {
            throw new AttributeInactiveException("Cannot assign a value to an inactive attribute.");
        }
        return attribute;
    }

    // ⭐ (Future Improvement Noted: Move to AttributeValueValidator in V2)
    private void validateValueByDataType(String value, AttributeDataType dataType) {
        if (value == null || value.isEmpty()) {
            throw new InvalidAttributeValueException("Value cannot be blank.");
        }

        switch (dataType) {
            case TEXT -> {
                // ⭐ IMPROVEMENT 3: Safety limit to prevent memory abuse
                if (value.length() > 100) {
                    throw new InvalidAttributeValueException("Text value cannot exceed 100 characters.");
                }
            }
            case INTEGER -> {
                try {
                    Integer.parseInt(value);
                } catch (NumberFormatException ex) {
                    throw new InvalidAttributeValueException("Value must be a valid whole number.");
                }
            }
            case DECIMAL -> {
                try {
                    new BigDecimal(value);
                } catch (Exception ex) {
                    throw new InvalidAttributeValueException("Value must be a valid decimal.");
                }
            }
            case BOOLEAN -> {
                if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                    throw new InvalidAttributeValueException("Value must be either true or false.");
                }
            }
            case DATE -> {
                // ⭐ IMPROVEMENT 4: Added explicit ISO formatter
                try {
                    java.time.LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (Exception ex) {
                    throw new InvalidAttributeValueException("Invalid date format. Expected yyyy-MM-dd.");
                }
            }
            case COLOR -> {
                // Version 1
                // Accept any non-blank value.
                // TODO: Later we can validate HEX (#FFFFFF) or RGB.
            }
            case EMAIL -> {
                // ⭐ IMPROVEMENT 5: Regex acceptable for V1
                if (!value.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                    throw new InvalidAttributeValueException("Invalid email format.");
                }
            }
            case URL -> {
                try {
                    URI uri = URI.create(value);

                    if (!"https".equalsIgnoreCase(uri.getScheme())) {
                        throw new InvalidAttributeValueException(
                                "Only HTTPS URLs are allowed.");
                    }

                    if (uri.getHost() == null || uri.getHost().isBlank()) {
                        throw new InvalidAttributeValueException("Invalid URL.");
                    }

                } catch (IllegalArgumentException ex) {
                    throw new InvalidAttributeValueException("Invalid URL.");
                }
            }
            case JSON -> {
                // ⭐ IMPROVEMENT 6: Kept simple
                // TODO: Add JSON syntax validation in Version 2
            }
            default -> throw new InvalidAttributeValueException("Unsupported attribute data type.");
        }
    }

    private AttributeValueResponse mapToResponse(AttributeValue attributeValue) {
        // ⭐ IMPROVEMENT 7: Noted for V2
        // TODO: In Version 2, expose attributeName (e.g., "Color") alongside the ID to improve the frontend experience.
        return AttributeValueResponse.builder()
                .id(attributeValue.getId())
                .attributeId(attributeValue.getAttribute().getId())
                .value(attributeValue.getValue())
                .isActive(attributeValue.getIsActive())
                .createdAt(attributeValue.getCreatedAt())
                .build();
    }
}