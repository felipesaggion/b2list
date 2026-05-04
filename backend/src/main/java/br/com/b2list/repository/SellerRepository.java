package br.com.b2list.repository;

import br.com.b2list.domain.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SellerRepository extends JpaRepository<Seller, UUID> {
    Seller findByExternalReferenceAndEnabledTrueAndTenantCode(String externalReference, String tenantCode);
}
