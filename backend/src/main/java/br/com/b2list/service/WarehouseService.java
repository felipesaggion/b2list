package br.com.b2list.service;

import br.com.b2list.domain.entity.Warehouse;

import java.util.List;
import java.util.UUID;

public interface WarehouseService {
    Warehouse save(Warehouse buyer);

    List<Warehouse> findAll();

    Warehouse findById(UUID id);

    Warehouse findByExternalReferenceAndEnabledTrueAndTenantCodeAndSellerId(String externalReference, String tenantCode, UUID sellerId);

    void deleteById(UUID id);
}
