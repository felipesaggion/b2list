package br.com.b2list.service.impl;

import br.com.b2list.domain.dto.OrderResponseDTO;
import br.com.b2list.domain.entity.Order;
import br.com.b2list.domain.entity.PaymentCondition;
import br.com.b2list.service.OrderDiscountStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component("TEN001Discount")
public class DefaultDiscountStrategy implements OrderDiscountStrategy {
    @Override
    public OrderResponseDTO.DiscountDTO apply(Order order, PaymentCondition rules) {
        OrderResponseDTO.DiscountDTO discountDTO = new OrderResponseDTO.DiscountDTO();

        // 5% se total > 500
        if (order.getTotal().compareTo(new BigDecimal("500.00")) > 0) {
            order.setDiscountValue(order.getTotal().multiply(rules.getDiscountPercentage().divide(new BigDecimal(100), RoundingMode.HALF_EVEN)));
            discountDTO.setPercentage(rules.getDiscountPercentage());
            discountDTO.setValue(order.getDiscountValue());
        }
        // Frete grátis se > 1000
        if (order.getDiscountValue().compareTo(rules.getFreeShippingThreshold()) >= 0) {
            discountDTO.setFreeShipping(true);
        }
        if (order.getDiscountValue().compareTo(BigDecimal.ZERO) > 0) {
            discountDTO.setDescription("Desconto aplicado: " + discountDTO.getPercentage() + "%");
        } else {
            discountDTO.setDescription("Nenhum desconto aplicável");
        }
        order.setTotal(order.getSubtotal().subtract(order.getDiscountValue()));
        return discountDTO;
    }
}