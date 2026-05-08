package br.com.b2list.repository;

import br.com.b2list.domain.dto.TopBuyerDTO;
import br.com.b2list.domain.entity.Buyer;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BuyerRepository extends JpaRepository<Buyer, UUID> {
    Buyer findByExternalReferenceAndEnabledTrueAndTenantCode(String externalReference, String tenantCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Buyer b WHERE b.id = :id")
    Optional<Buyer> findByIdWithLock(@Param("id") UUID id);

    @Query("""
                SELECT new br.com.b2list.domain.dto.TopBuyerDTO(
                    b.name,
                    COUNT(o.id) as orderCount,
                    SUM(o.total) as totalSpent
                )
                FROM Order o
                JOIN o.buyer b
                WHERE o.tenantCode = :tenant
                  AND o.createdAt BETWEEN :from AND :to
                  AND o.status = 'COMPLETED'
                GROUP BY b.name
                ORDER BY SUM(o.total) DESC LIMIT 5
            """)
    List<TopBuyerDTO> findTopBuyers(String tenant, OffsetDateTime from, OffsetDateTime to);

    List<Buyer> findAllByTenantCodeAndEnabledTrue(String tenantCode);
}

