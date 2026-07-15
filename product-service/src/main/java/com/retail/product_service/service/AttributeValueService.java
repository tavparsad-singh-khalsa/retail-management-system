package com.retail.product_service.service;

import com.retail.product_service.dto.request.CreateAttributeValueRequest;
import com.retail.product_service.dto.request.UpdateAttributeValueRequest;
import com.retail.product_service.dto.response.AttributeValueResponse;

import java.util.List;

public interface AttributeValueService {

    AttributeValueResponse createAttributeValue(
            CreateAttributeValueRequest request
    );

    AttributeValueResponse updateAttributeValue(
            Long id,
            UpdateAttributeValueRequest request
    );

    AttributeValueResponse getAttributeValueById(Long id);

    List<AttributeValueResponse> getAllAttributeValues();

    List<AttributeValueResponse> getValuesByAttribute(Long attributeId);

    void activateAttributeValue(Long id);

    void deactivateAttributeValue(Long id);

}
