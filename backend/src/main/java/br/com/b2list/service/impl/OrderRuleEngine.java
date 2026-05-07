package br.com.b2list.service.impl;

import br.com.b2list.domain.dto.OrderResponseDTO;
import br.com.b2list.domain.entity.Order;
import br.com.b2list.domain.entity.PaymentCondition;
import br.com.b2list.service.OrderDiscountStrategy;
import br.com.b2list.service.OrderPricingStrategy;
import br.com.b2list.service.OrderValidationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderRuleEngine {

    private final StrategyFactory strategyFactory;

    public OrderResponseDTO.DiscountDTO processOrder(Order order, String tenantCode, String paymentCode) {
        PaymentCondition rules = strategyFactory.resolveRules(tenantCode, paymentCode);

        OrderValidationStrategy validator = strategyFactory.getValidationStrategy(tenantCode);
        OrderPricingStrategy pricer = strategyFactory.getPricingStrategy(tenantCode);
        OrderDiscountStrategy discounter = strategyFactory.getDiscountStrategy(tenantCode);

        validator.validate(order, rules);
        pricer.calculate(order, rules);
        return discounter.apply(order, rules);
    }
}