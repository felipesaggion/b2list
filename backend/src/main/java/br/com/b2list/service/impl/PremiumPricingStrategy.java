package br.com.b2list.service.impl;

import br.com.b2list.domain.entity.Order;
import br.com.b2list.domain.entity.PaymentCondition;
import br.com.b2list.service.OrderPricingStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component("TEN002Pricing")
public class PremiumPricingStrategy implements OrderPricingStrategy {

    @Override
    public void calculate(Order order, PaymentCondition rules) {
        // Regra: Subtotal padrão (Soma de itens já deve vir calculada no objeto Order)
        // Garante que não existam taxas operacionais
        if (rules.getOperationalFeePercentage().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal operationalFeePerc = rules.getOperationalFeePercentage()
                    .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

            BigDecimal feeAmount = order.getSubtotal().multiply(operationalFeePerc)
                    .setScale(2, RoundingMode.HALF_UP);

            // Define a taxa no pedido para transparência (exibição no checkout)
            order.setOperationalFee(feeAmount);

            // Atualiza o valor total: Subtotal + Taxas
            // Este valor atualizado servirá de base para a OrderDiscountStrategy
            order.setTotal(order.getSubtotal().add(feeAmount));
        }

    }
}