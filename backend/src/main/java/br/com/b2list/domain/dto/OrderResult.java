package br.com.b2list.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class OrderResult {
    private BigDecimal total;
    private OrderResponseDTO.DiscountDTO discountDTO;
    private Boolean allowBonusOrder;

    public OrderResult(BigDecimal total, Boolean allowBonusOrder, OrderResponseDTO.DiscountDTO discountDTO) {
        this.total = total;
        this.allowBonusOrder = allowBonusOrder;
        this.discountDTO = discountDTO;
    }

}