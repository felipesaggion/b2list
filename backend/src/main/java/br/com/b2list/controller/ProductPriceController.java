package br.com.b2list.controller;

import br.com.b2list.domain.dto.ProductPriceDTO;
import br.com.b2list.enums.Error;
import br.com.b2list.exception.TenantNotFoundException;
import br.com.b2list.service.ProductPriceService;
import br.com.b2list.tenant.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/product-price")
public class ProductPriceController {

    @Autowired
    private ProductPriceService sellerService;

    @GetMapping("/warehouse/{externalReference}")
    public List<ProductPriceDTO> findAll(@PathVariable String externalReference) throws TenantNotFoundException {
        String tenant = TenantContext.getTenant();
        if (tenant == null) {
            throw new TenantNotFoundException("Tenant não encontrado nos headers", Error.ORD_VALIDATION_005);
        }
        return sellerService.findByTenantCodeAndWarehouseExternalReferenceEnabledTrue(tenant, externalReference);
    }

    @GetMapping("/{id}")
    public ProductPriceDTO findById(@PathVariable UUID id) {
        return sellerService.findById(id);
    }

    @PostMapping
    public ProductPriceDTO save(@RequestBody ProductPriceDTO seller) {
        return sellerService.save(seller);
    }

    @PutMapping
    public ProductPriceDTO update(@RequestBody ProductPriceDTO seller) {
        return sellerService.save(seller);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable UUID id) {
        sellerService.deleteById(id);
    }
}
