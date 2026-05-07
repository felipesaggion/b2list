package br.com.b2list.service.impl;

import br.com.b2list.domain.dto.OrderResponseDTO;
import br.com.b2list.domain.entity.Order;
import br.com.b2list.domain.entity.PaymentCondition;
import br.com.b2list.service.OrderDiscountStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component("TEN002Discount")
public class PremiumDiscountStrategy implements OrderDiscountStrategy {
    @Override
    public OrderResponseDTO.DiscountDTO apply(Order order, PaymentCondition rules) {
        // Soma 8% (base) + 3% (extra se for CASH) vindos do banco
        BigDecimal totalDiscountPerc = rules.getDiscountPercentage().add(rules.getExtraDiscountPercentage());
        order.setDiscountValue(order.getTotal().multiply(totalDiscountPerc.divide(new BigDecimal(100), RoundingMode.HALF_EVEN)));
        OrderResponseDTO.DiscountDTO discountDTO = new OrderResponseDTO.DiscountDTO();
        discountDTO.setPercentage(totalDiscountPerc);
        discountDTO.setValue(order.getDiscountValue());
        // Frete Grátis sempre (threshold 0.00 no banco)
        discountDTO.setFreeShipping(true);
        discountDTO.setDescription("Desconto aplicado: " + discountDTO.getPercentage() + "%");
        order.setTotal(order.getTotal().subtract(order.getDiscountValue()));
        return discountDTO;
    }
}