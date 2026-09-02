package com.retail.reportservice.client;

import com.retail.reportservice.dto.external.ExternalInventoryDto;
import com.retail.reportservice.dto.external.ExternalProductDto;

import java.util.List;

public interface ProductClient {

    List<ExternalProductDto> getAllProducts();

    List<ExternalInventoryDto> getAllInventory();
}
