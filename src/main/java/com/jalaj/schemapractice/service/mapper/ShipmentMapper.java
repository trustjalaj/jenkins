package com.jalaj.schemapractice.service.mapper;

import com.jalaj.schemapractice.domain.*;
import com.jalaj.schemapractice.service.dto.ShipmentDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Shipment and its DTO ShipmentDTO.
 */
@Mapper(componentModel = "spring", uses = {InvoiceMapper.class})
public interface ShipmentMapper extends EntityMapper<ShipmentDTO, Shipment> {

    @Mapping(source = "invoice.id", target = "invoiceId")
    ShipmentDTO toDto(Shipment shipment);

    @Mapping(source = "invoiceId", target = "invoice")
    Shipment toEntity(ShipmentDTO shipmentDTO);

    default Shipment fromId(Long id) {
        if (id == null) {
            return null;
        }
        Shipment shipment = new Shipment();
        shipment.setId(id);
        return shipment;
    }
}
