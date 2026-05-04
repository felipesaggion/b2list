package br.com.b2list.projection;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface OrderListingProjection {
    UUID getOrderId();
    String getExternalReference();
    String getBuyerName();
    String getSellerName();
    String getWarehouseName();
    String getStatus();
    BigDecimal getSubtotal();
    BigDecimal getDiscountValue();
    BigDecimal getTotal();
    Integer getItemCount();
    String getOrigin();
    OffsetDateTime getCreatedAt();
}