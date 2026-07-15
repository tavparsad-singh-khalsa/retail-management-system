package com.retail.product_service.service;

import com.retail.product_service.dto.request.CreateAttributeRequest;
import com.retail.product_service.dto.request.UpdateAttributeRequest;
import com.retail.product_service.dto.response.AttributeResponse;

import java.util.List;
public interface AttributeService {

    AttributeResponse createAttribute(CreateAttributeRequest request);

    AttributeResponse updateAttribute(Long id, UpdateAttributeRequest request);

    AttributeResponse getAttributeById(Long id);

    List<AttributeResponse> getAllAttributes();

    void activateAttribute(Long id);

    void deactivateAttribute(Long id);
}
