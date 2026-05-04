package br.com.b2list.service;

import br.com.b2list.domain.dto.OrderResult;
import br.com.b2list.domain.entity.Order;
import br.com.b2list.domain.entity.PaymentCondition;

public interface PaymentCalculator {
    OrderResult process(Order order, PaymentCondition condition);
}