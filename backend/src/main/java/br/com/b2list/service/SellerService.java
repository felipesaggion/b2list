package br.com.b2list.service;

import br.com.b2list.domain.dto.SellerDTO;
import br.com.b2list.domain.entity.Seller;

import java.util.List;
import java.util.UUID;

public interface SellerService {
    SellerDTO save(SellerDTO buyer);

    List<SellerDTO> findByTenantCodeAndEnabledTrue(String tenantCode);

    SellerDTO findById(UUID id);

    Seller findByExternalReferenceAndEnabledTrueAndTenantCode(String externalReference, String tenantCode);

    void deleteById(UUID id);

}
