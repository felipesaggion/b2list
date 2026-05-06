package br.com.b2list.service.impl;

import br.com.b2list.domain.dto.ProductPriceDTO;
import br.com.b2list.domain.dto.TopProductDTO;
import br.com.b2list.domain.entity.ProductPrice;
import br.com.b2list.mapper.ProductPriceMapper;
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

    @Autowired
    private ProductPriceMapper productPriceMapper;

    @Override
    public ProductPriceDTO save(ProductPriceDTO productPriceDTO) {
        ProductPrice productPrice = productPriceRepository.findByProductCodeAndTenantCodeAndWarehouseIdAndEnabledTrue(
                productPriceDTO.getProductCode(),
                productPriceDTO.getTenantCode(),
                productPriceDTO.getWarehouseId()
        );
        UUID id = productPrice.getId();
        if (id == null) {
            productPriceDTO.setTenantCode(TenantContext.getTenant());
            productPriceDTO.setLastModified(OffsetDateTime.now());
        }
        productPrice = productPriceMapper.toEntity(productPriceDTO);
        productPrice.setId(id);
        return productPriceMapper.toDto(productPriceRepository.save(productPrice));
    }

    @Override
    public List<ProductPriceDTO> findAll() {
        return productPriceRepository.findAll().stream().map(productPriceMapper::toDto).toList();
    }

    @Override
    public ProductPriceDTO findById(UUID id) {
        return productPriceMapper.toDto(productPriceRepository.findById(id).orElse(null));
    }

    @Override
    public ProductPrice findByProductCodeAndTenantCodeAndWarehouseIdAndEnabledTrue(String code, String tenantCode, UUID warehouseId) {
        return productPriceRepository.findByProductCodeAndTenantCodeAndWarehouseIdAndEnabledTrue(code, tenantCode, warehouseId);
    }

    @Override
    public void deleteById(UUID id) {
        productPriceRepository.deleteById(id);
    }

    @Override
    public List<TopProductDTO> findTopProducts(String tenant, OffsetDateTime from, OffsetDateTime to) {
        return productPriceRepository.findTopProducts(tenant, from, to);
    }
}
