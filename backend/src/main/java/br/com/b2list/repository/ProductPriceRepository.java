package br.com.b2list.repository;

import br.com.b2list.domain.dto.TopProductDTO;
import br.com.b2list.domain.entity.ProductPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface ProductPriceRepository extends JpaRepository<ProductPrice, UUID> {
    ProductPrice findByProductCodeAndTenantCodeAndWarehouseIdAndEnabledTrue(String code, String tenantCode, UUID warehouseId);

    @Query("""
                SELECT new br.com.b2list.domain.dto.TopProductDTO(
                    i.productCode,
                    i.productName,
                    SUM(i.quantity) as totalQuantity
                )
                FROM OrderItem i
                JOIN i.order o
                WHERE o.tenantCode = :tenant
                  AND o.createdAt BETWEEN :from AND :to
                  AND o.status = 'COMPLETED'
                GROUP BY i.productCode, i.productName
                ORDER BY SUM(i.quantity) DESC LIMIT 5
            """)
    List<TopProductDTO> findTopProducts(String tenant, OffsetDateTime from, OffsetDateTime to);

    @Query("""
                SELECT p
                FROM ProductPrice p
                WHERE p.tenantCode = :tenantCode
                  AND p.enabled = true
                  AND p.warehouse.id = (SELECT w.id FROM Warehouse w WHERE w.externalReference = :externalReference)
            """)
    List<ProductPrice> findByTenantCodeAndWarehouseExternalReferenceEnabledTrue(String tenantCode, String externalReference);
}
