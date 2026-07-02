package com.retail.product_service.repository;

import com.retail.product_service.entity.AttributeValue;
import com.retail.product_service.entity.ProductVariant;
import com.retail.product_service.entity.VariantAttribute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VariantAttributeRepository extends JpaRepository<VariantAttribute , Long> {

    List<VariantAttribute> findByProductVariant(ProductVariant productVariant);

    List<VariantAttribute> findByAttributeValue(AttributeValue attributeValue);
}
