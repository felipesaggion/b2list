package br.com.b2list.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class StatisticsDTO {
    private String tenant;
    private PeriodDTO period;
    private Long totalOrders;
    private Long confirmedOrders;
    private Long cancelledOrders;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private List<TopBuyerDTO> topBuyers;
    private List<TopProductDTO> topProducts;
}