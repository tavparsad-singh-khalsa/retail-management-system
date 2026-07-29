package com.retail.product_service.repository;

import com.retail.product_service.entity.Attribute;
import com.retail.product_service.entity.AttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttributeValueRepository extends JpaRepository<AttributeValue , Long> {

    List<AttributeValue> findByAttribute(Attribute attribute);

    List<AttributeValue> findByValueContainingIgnoreCase(String value);

    boolean existsByAttributeAndIsActiveTrue(Attribute attribute);

    List<AttributeValue> findByAttributeAndIsActiveTrue(Attribute attribute);

    boolean existsByAttributeAndValueIgnoreCase(
            Attribute attribute,
            String value
    );

    Optional<AttributeValue> findByAttributeAndValueIgnoreCase(
            Attribute attribute,
            String value
    );

    List<AttributeValue> findByAttributeId(Long attributeId);
}
