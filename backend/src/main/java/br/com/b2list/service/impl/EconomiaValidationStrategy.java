package br.com.b2list.service.impl;

import br.com.b2list.domain.entity.Order;
import br.com.b2list.domain.entity.PaymentCondition;
import br.com.b2list.service.OrderValidationStrategy;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.ZoneId;

@Component("TEN003Validation")
public class EconomiaValidationStrategy implements OrderValidationStrategy {
    @Override
    public void validate(Order order, PaymentCondition rules) {
        // Regra: Bloqueia fora do horário comercial (8h-18h UTC-3)
        if (Boolean.TRUE.equals(rules.getAllowOnlyBusinessHours())) {
            LocalTime now = LocalTime.now(ZoneId.of("America/Sao_Paulo"));
            if (now.isBefore(LocalTime.of(8, 0)) || now.isAfter(LocalTime.of(18, 0))) {
                throw new RuntimeException("Pedidos para o tenant ECONOMIA só são permitidos entre 08:00 e 18:00.");
            }
        }

        // Regra: Pedido mínimo de R$ 200,00
        if (order.getSubtotal().compareTo(rules.getMinValue()) < 0) {
            throw new RuntimeException("Tenant ECONOMIA exige pedido mínimo de R$ " + rules.getMinValue());
        }

        // Regra: Máximo 50 itens
        if (order.getItems().size() > rules.getMaxItems()) {
            throw new RuntimeException("Limite restrito de itens excedido: " + rules.getMaxItems());
        }
    }
}