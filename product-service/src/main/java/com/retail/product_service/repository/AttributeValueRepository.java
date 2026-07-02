package com.retail.product_service.repository;

import com.retail.product_service.entity.Attribute;
import com.retail.product_service.entity.AttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttributeValueRepository extends JpaRepository<AttributeValue , Long> {

    List<AttributeValue> findByAttribute(Attribute attribute);

    List<AttributeValue> findByValueContainingIgnoreCase(String value);
}
