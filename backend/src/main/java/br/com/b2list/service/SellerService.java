package br.com.b2list.service;

import br.com.b2list.domain.entity.Buyer;
import br.com.b2list.domain.entity.Seller;

import java.util.List;
import java.util.UUID;

public interface SellerService {
    Seller save(Seller buyer);
    List<Seller> findAll();
    Seller findById(UUID id);
    Seller findByExternalReferenceAndEnabledTrueAndTenantCode(String externalReference, String tenantCode);
    void deleteById(UUID id);

}
