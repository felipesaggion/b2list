package br.com.b2list.projection;

import java.math.BigDecimal;

public interface OrderSummaryProjection {
    Long getTotalOrders();
    Long getConfirmedOrders();
    Long getCancelledOrders();
    BigDecimal getTotalRevenue();
    BigDecimal getAverageOrderValue();
}