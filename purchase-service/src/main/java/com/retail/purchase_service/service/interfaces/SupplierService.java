package com.retail.purchase_service.service.interfaces;

import com.retail.purchase_service.dto.request.CreateSupplierRequest;
import com.retail.purchase_service.dto.request.UpdateSupplierRequest;
import com.retail.purchase_service.dto.response.SupplierResponse;

import java.util.List;

public interface SupplierService {

    SupplierResponse createSupplier(CreateSupplierRequest request);

    SupplierResponse updateSupplier(Long id,
                                    UpdateSupplierRequest request);

    SupplierResponse getSupplierById(Long id);

    List<SupplierResponse> getAllSuppliers();

    List<SupplierResponse> getAllActiveSuppliers();

    void activateSupplier(Long id);

    void deactivateSupplier(Long id);
}
