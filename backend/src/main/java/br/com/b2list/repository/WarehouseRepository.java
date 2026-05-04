package br.com.b2list.repository;

import br.com.b2list.domain.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {
    Warehouse findByExternalReferenceAndEnabledTrueAndTenantCodeAndSellerId(
            String externalReference,
            String tenantCode,
            UUID sellerId
    );
}
