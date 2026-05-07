package br.com.b2list.service;

import br.com.b2list.domain.dto.OrderResponseDTO;
import br.com.b2list.domain.entity.Order;
import br.com.b2list.domain.entity.PaymentCondition;

public interface OrderDiscountStrategy {
    OrderResponseDTO.DiscountDTO apply(Order order, PaymentCondition rules);
}