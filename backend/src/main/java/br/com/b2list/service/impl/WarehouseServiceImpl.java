package br.com.b2list.service.impl;

import br.com.b2list.domain.dto.WarehouseDTO;
import br.com.b2list.domain.entity.Warehouse;
import br.com.b2list.mapper.WarehouseMapper;
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

    @Autowired
    private WarehouseMapper warehouseMapper;

    @Override
    public WarehouseDTO save(WarehouseDTO warehouseDTO) {
        warehouseDTO.setTenantCode(TenantContext.getTenant());
        Warehouse warehouse = warehouseRepository.findByExternalReferenceAndEnabledTrueAndTenantCodeAndSellerId(
                warehouseDTO.getExternalReference(),
                warehouseDTO.getTenantCode(),
                warehouseDTO.getSellerId()
        );
        UUID id = warehouse.getId();
        if (id == null) {
            warehouseDTO.setCreatedAt(OffsetDateTime.now());
        }
        warehouse = warehouseMapper.toEntity(warehouseDTO);
        warehouse.setId(id);
        return warehouseMapper.toDto(warehouseRepository.save(warehouse));
    }

    @Override
    public List<WarehouseDTO> findAllByTenantCodeAndSellerExternalReferenceAndEnabledTrue(String tenant, String externalReference) {
        return warehouseRepository.findAllByTenantCodeAndSellerIdAndEnabledTrue(tenant, externalReference)
                .stream().map(warehouseMapper::toDto).toList();
    }

    @Override
    public WarehouseDTO findById(UUID id) {
        return warehouseMapper.toDto(warehouseRepository.findById(id).orElse(null));
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
