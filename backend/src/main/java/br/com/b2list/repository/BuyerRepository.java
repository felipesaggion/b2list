package br.com.b2list.repository;

import br.com.b2list.domain.entity.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

public interface BuyerRepository extends JpaRepository<Buyer, UUID> {
    Buyer findByExternalReferenceAndEnabledTrueAndTenantCode(String externalReference, String tenantCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Buyer b WHERE b.id = :id")
    Optional<Buyer> findByIdWithLock(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Buyer b WHERE b.id = :id AND b.tenantCode = :tenantCode")
    Optional<Buyer> findByIdAndTenantCodeWithLock(@Param("id") UUID id, @Param("tenantCode") String tenantCode);
}
