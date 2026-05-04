package br.com.b2list.service.impl;

import br.com.b2list.domain.entity.Warehouse;
import br.com.b2list.repository.WarehouseRepository;
import br.com.b2list.service.WarehouseService;
import br.com.b2list.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class WarehouseServiceImpl implements WarehouseService {

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Override
    public Warehouse save(Warehouse warehouse) {
        warehouse.setTenantCode(TenantContext.getTenant());
        if (warehouse.getId() == null) {
            warehouse.setCreatedAt(OffsetDateTime.now());
        }
        return warehouseRepository.save(warehouse);
    }

    @Override
    public List<Warehouse> findAll() {
        return warehouseRepository.findAll();
    }

    @Override
    public Warehouse findById(UUID id) {
        return warehouseRepository.findById(id).orElse(null);
    }

    @Override
    public Warehouse findByExternalReferenceAndEnabledTrueAndTenantCodeAndSellerId(String externalReference, String tenantCode, UUID sellerId) {
        return warehouseRepository.findByExternalReferenceAndEnabledTrueAndTenantCodeAndSellerId(
                externalReference,
                tenantCode,
                sellerId
        );
    }

    @Override
    public void deleteById(UUID id) {
        warehouseRepository.deleteById(id);
    }
}
