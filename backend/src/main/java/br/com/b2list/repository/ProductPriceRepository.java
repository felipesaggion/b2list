package br.com.b2list.repository;

import br.com.b2list.domain.entity.ProductPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductPriceRepository extends JpaRepository<ProductPrice, UUID> {
    ProductPrice findByProductCodeAndTenantCodeAndWarehouseIdAndEnabledTrue(String code, String tenantCode, UUID warehouseId);
}
