package com.retail.product_service.service;

import com.retail.product_service.dto.request.CreateVariantAttributeRequest;
import com.retail.product_service.dto.request.UpdateVariantAttributeRequest;
import com.retail.product_service.dto.response.VariantAttributeResponse;

import java.util.List;

public interface VariantAttributeService {

    VariantAttributeResponse createVariantAttribute(CreateVariantAttributeRequest request);

    VariantAttributeResponse updateVariantAttribute(Long id, UpdateVariantAttributeRequest request);

    VariantAttributeResponse getVariantAttributeById(Long id);

    List<VariantAttributeResponse> getVariantAttributesByVariantId(Long productVariantId);

    void activateVariantAttribute(Long id);

    void deactivateVariantAttribute(Long id);

    List<VariantAttributeResponse> getAllVariantAttributes();
}