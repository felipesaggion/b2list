package br.com.b2list.service;

import br.com.b2list.domain.dto.ProductPriceDTO;
import br.com.b2list.domain.dto.TopProductDTO;
import br.com.b2list.domain.entity.ProductPrice;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface ProductPriceService {
    ProductPriceDTO save(ProductPriceDTO productPrice);

    List<ProductPriceDTO> findAll();

    ProductPriceDTO findById(UUID id);

    ProductPrice findByProductCodeAndTenantCodeAndWarehouseIdAndEnabledTrue(String code, String tenantCode, UUID warehouseId);

    void deleteById(UUID id);

    List<TopProductDTO> findTopProducts(String tenant, OffsetDateTime from, OffsetDateTime to);


}
