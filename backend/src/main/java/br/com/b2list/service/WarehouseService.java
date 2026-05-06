package br.com.b2list.service;

import br.com.b2list.domain.dto.WarehouseDTO;
import br.com.b2list.domain.entity.Warehouse;

import java.util.List;
import java.util.UUID;

public interface WarehouseService {
    WarehouseDTO save(WarehouseDTO buyer);

    List<WarehouseDTO> findAll();

    WarehouseDTO findById(UUID id);

    Warehouse findByExternalReferenceAndEnabledTrueAndTenantCodeAndSellerId(String externalReference, String tenantCode, UUID sellerId);

    void deleteById(UUID id);
}
