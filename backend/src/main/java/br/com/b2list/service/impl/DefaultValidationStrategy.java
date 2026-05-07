package br.com.b2list.service.impl;

import br.com.b2list.domain.entity.Order;
import br.com.b2list.domain.entity.PaymentCondition;
import br.com.b2list.service.OrderValidationStrategy;
import org.springframework.stereotype.Component;

@Component("TEN001Validation")
public class DefaultValidationStrategy implements OrderValidationStrategy {
    @Override
    public void validate(Order order, PaymentCondition rules) {
        // Regra: Pedido mínimo de R$ 50,00
        if (order.getSubtotal().compareTo(rules.getMinValue()) < 0) {
            throw new RuntimeException("Pedido mínimo não atingido. Valor mínimo: R$ " + rules.getMinValue());
        }

        // Regra: Máximo 100 itens
        if (order.getItems().size() > rules.getMaxItems()) {
            throw new RuntimeException("Quantidade de itens excede o limite de " + rules.getMaxItems());
        }
    }
}