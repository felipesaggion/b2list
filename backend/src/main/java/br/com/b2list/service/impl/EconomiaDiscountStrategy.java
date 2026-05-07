package br.com.b2list.service.impl;

import br.com.b2list.domain.dto.OrderResponseDTO;
import br.com.b2list.domain.entity.Order;
import br.com.b2list.domain.entity.PaymentCondition;
import br.com.b2list.service.OrderDiscountStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component("TEN003Discount")
public class EconomiaDiscountStrategy implements OrderDiscountStrategy {
    @Override
    public OrderResponseDTO.DiscountDTO apply(Order order, PaymentCondition rules) {
        // Aplica apenas o extra_discount_percentage (ex: 2% se for CASH)
        order.setDiscountValue(order.getSubtotal().multiply(rules.getExtraDiscountPercentage().divide(new BigDecimal(100), RoundingMode.HALF_EVEN)));
        OrderResponseDTO.DiscountDTO discountDTO = new OrderResponseDTO.DiscountDTO();
        discountDTO.setValue(order.getDiscountValue());
        discountDTO.setPercentage(rules.getExtraDiscountPercentage());
        discountDTO.setDescription("Desconto aplicado: " + discountDTO.getPercentage() + "%");
        // Sem frete grátis (threshold será 999999.99 no banco)
        discountDTO.setFreeShipping(false);
        return discountDTO;
    }
}