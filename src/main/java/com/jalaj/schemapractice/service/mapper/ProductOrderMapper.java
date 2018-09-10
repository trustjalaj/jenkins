package com.jalaj.schemapractice.service.mapper;

import com.jalaj.schemapractice.domain.*;
import com.jalaj.schemapractice.service.dto.ProductOrderDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity ProductOrder and its DTO ProductOrderDTO.
 */
@Mapper(componentModel = "spring", uses = {CustomerMapper.class})
public interface ProductOrderMapper extends EntityMapper<ProductOrderDTO, ProductOrder> {

    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "customer.email", target = "customerEmail")
    ProductOrderDTO toDto(ProductOrder productOrder);

    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "invoices", ignore = true)
    @Mapping(source = "customerId", target = "customer")
    ProductOrder toEntity(ProductOrderDTO productOrderDTO);

    default ProductOrder fromId(Long id) {
        if (id == null) {
            return null;
        }
        ProductOrder productOrder = new ProductOrder();
        productOrder.setId(id);
        return productOrder;
    }
}
