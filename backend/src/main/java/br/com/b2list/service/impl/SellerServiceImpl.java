package br.com.b2list.service.impl;

import br.com.b2list.domain.entity.Seller;
import br.com.b2list.repository.SellerRepository;
import br.com.b2list.service.SellerService;
import br.com.b2list.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SellerServiceImpl implements SellerService {
    @Autowired
    private SellerRepository sellerRepository;

    @Override
    public Seller save(Seller seller) {
        seller.setTenantCode(TenantContext.getTenant());
        if (seller.getId() == null) {
            seller.setCreatedAt(OffsetDateTime.now());
        }
        return sellerRepository.save(seller);
    }

    @Override
    public List<Seller> findAll() {
        return sellerRepository.findAll();
    }

    @Override
    public Seller findById(UUID id) {
        return sellerRepository.findById(id).orElse(null);
    }

    @Override
    public Seller findByExternalReferenceAndEnabledTrueAndTenantCode(String externalReference, String tenantCode) {
        return sellerRepository.findByExternalReferenceAndEnabledTrueAndTenantCode(
                externalReference,
                tenantCode
        );
    }

    @Override
    public void deleteById(UUID id) {
        sellerRepository.deleteById(id);
    }
}
