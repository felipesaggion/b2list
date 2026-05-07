package br.com.b2list.service.impl;

import br.com.b2list.domain.entity.Order;
import br.com.b2list.domain.entity.PaymentCondition;
import br.com.b2list.service.OrderPricingStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("TEN001Pricing")
public class DefaultPricingStrategy implements OrderPricingStrategy {

    @Override
    public void calculate(Order order, PaymentCondition rules) {
        // Regra: Subtotal padrão (Soma de itens já deve vir calculada no objeto Order)
        // Garante que não existam taxas operacionais
        order.setOperationalFee(BigDecimal.ZERO);

        // O valor base para o cálculo de descontos será o subtotal puro
        order.setTotal(order.getSubtotal());
    }
}