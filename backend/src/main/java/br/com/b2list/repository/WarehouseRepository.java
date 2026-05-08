package br.com.b2list.repository;

import br.com.b2list.domain.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {
    Warehouse findByExternalReferenceAndEnabledTrueAndTenantCodeAndSellerId(
            String externalReference,
            String tenantCode,
            UUID sellerId
    );
    @Query("""
    SELECT w FROM Warehouse w
        WHERE w.tenantCode = :tenantCode
          AND w.seller.id = (SELECT s.id FROM Seller s WHERE s.externalReference = :sellerExternalReference AND s.tenantCode = :tenantCode AND s.enabled = true)
          AND w.enabled = true
    """)
    List<Warehouse> findAllByTenantCodeAndSellerIdAndEnabledTrue(String tenantCode, String sellerExternalReference);
}
