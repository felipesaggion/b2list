package br.com.b2list.service.impl;

import br.com.b2list.domain.entity.Order;
import br.com.b2list.domain.entity.PaymentCondition;
import br.com.b2list.service.OrderValidationStrategy;
import org.springframework.stereotype.Component;

@Component("TEN002Validation")
public class PremiumValidationStrategy implements OrderValidationStrategy {
    @Override
    public void validate(Order order, PaymentCondition rules) {
        // Regra: Sem valor mínimo (rules.getMinValue() deve ser 0 no banco)
        
        // Regra: Máximo 500 itens
        if (order.getItems().size() > rules.getMaxItems()) {
            throw new RuntimeException("Limite Premium de itens excedido: " + rules.getMaxItems());
        }
    }
}