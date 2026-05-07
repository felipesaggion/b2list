package br.com.b2list.service;

import br.com.b2list.domain.entity.Order;
import br.com.b2list.domain.entity.PaymentCondition;

public interface OrderValidationStrategy {
    void validate(Order order, PaymentCondition rules);
}