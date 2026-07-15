package com.retail.product_service.service;

import com.retail.product_service.dto.request.CreateBrandRequest;
import com.retail.product_service.dto.request.UpdateBrandRequest;
import com.retail.product_service.dto.response.BrandResponse;

import java.util.List;

public interface BrandService {

        BrandResponse createBrand(CreateBrandRequest request);
        BrandResponse updateBrand(Long id, UpdateBrandRequest request);
        BrandResponse getBrandById(Long id);
        List<BrandResponse> getAllBrands();
        void activateBrand(Long id);
        void deactivateBrand(Long id);
        List<BrandResponse> getAllActiveBrands();
}
