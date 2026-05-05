package br.com.b2list.event;

import br.com.b2list.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPayload {
    private UUID orderId;
    private String externalReference;
    private String buyerReference;
    private BigDecimal total;
    private OrderStatus status;
}