package br.com.b2list.repository;

import br.com.b2list.domain.entity.Order;
import br.com.b2list.enums.OrderStatus;
import br.com.b2list.projection.OrderListingProjection;
import br.com.b2list.projection.OrderSummaryProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("""
            SELECT o.id as orderId,
                   o.externalReference as externalReference,
                   b.name as buyerName,
                   s.name as sellerName,
                   wh.name as warehouseName,
                   o.status as status,
                   o.subtotal as subtotal,
                   o.discountValue as discountValue,
                   o.total as total,
                   COALESCE(SUM(oi.quantity), 0L) as itemCount,
                   o.origin as origin,
                   o.createdAt as createdAt
            FROM Order o
            INNER JOIN o.buyer b
            INNER JOIN o.seller s
            INNER JOIN o.warehouse wh
            LEFT JOIN o.items oi
            WHERE (:tenantCode IS NULL OR o.tenantCode = :tenantCode)
            AND (o.createdAt >= :startDate)
            AND (o.createdAt <= :endDate)
            AND o.status = :status
            AND (:buyerRef IS NULL OR b.externalReference = :buyerRef)
            GROUP BY o.id, b.id, s.id, wh.id
            ORDER BY o.externalReference ASC
            """)
    Page<OrderListingProjection> findByStarDateAndEndDateAndTenantCode(
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate,
            @Param("status") OrderStatus status,
            @Param("buyerRef") String buyerRef,
            @Param("tenantCode") String tenantCode,
            Pageable pageable);

    @Query("""
                SELECT COUNT(o) as totalOrders,
                       SUM(CASE WHEN o.status = 'COMPLETED' THEN 1 ELSE 0 END) as confirmedOrders,
                       SUM(CASE WHEN o.status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelledOrders,
                       SUM(o.total) as totalRevenue,
                       AVG(o.total) as averageOrderValue
                FROM Order o
                WHERE o.tenantCode = :tenant
                  AND o.createdAt BETWEEN :from AND :to
            """)
    OrderSummaryProjection getSummary(String tenant, OffsetDateTime from, OffsetDateTime to);

    Order findByExternalReferenceAndTenantCode(String externalReference, String tenantCode);
}
