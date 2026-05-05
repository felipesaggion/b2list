package br.com.b2list.domain.dto;

import br.com.b2list.enums.OrderOrigin;
import br.com.b2list.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
public class OrderDTO {
    private String code;
    private String externalReference;
    private BuyerDTO buyer;
    private SellerDTO seller;
    private WarehouseDTO warehouse;
    private PaymentConditionDTO paymentCondition;
    private OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal discountValue;
    private BigDecimal total;
    private OrderOrigin origin;
    private String tenantCode;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastModified;
    private Long version;
    private List<OrderItemDTO> items;

}