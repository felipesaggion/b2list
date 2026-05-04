package br.com.b2list.service.impl;

import br.com.b2list.domain.entity.ProductPrice;
import br.com.b2list.repository.ProductPriceRepository;
import br.com.b2list.service.ProductPriceService;
import br.com.b2list.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProductPriceServiceImpl implements ProductPriceService {

    @Autowired
    private ProductPriceRepository productPriceRepository;

    @Override
    public ProductPrice save(ProductPrice productPrice) {
        productPrice.setTenantCode(TenantContext.getTenant());
        productPrice.setLastModified(OffsetDateTime.now());
        return productPriceRepository.save(productPrice);
    }

    @Override
    public List<ProductPrice> findAll() {
        return productPriceRepository.findAll();
    }

    @Override
    public ProductPrice findById(UUID id) {
        return productPriceRepository.findById(id).orElse(null);
    }

    @Override
    public ProductPrice findByProductCodeAndTenantCodeAndWarehouseIdAndEnabledTrue(String code, String tenantCode, UUID warehouseId) {
        return productPriceRepository.findByProductCodeAndTenantCodeAndWarehouseIdAndEnabledTrue(code, tenantCode, warehouseId);
    }

    @Override
    public void deleteById(UUID id) {
        productPriceRepository.deleteById(id);
    }
}
