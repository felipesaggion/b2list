package br.com.b2list.service.impl;

import br.com.b2list.domain.dto.OrderResponseDTO;
import br.com.b2list.domain.dto.OrderResult;
import br.com.b2list.domain.entity.Order;
import br.com.b2list.domain.entity.OrderItem;
import br.com.b2list.domain.entity.PaymentCondition;
import br.com.b2list.service.PaymentCalculator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.time.ZoneId;

@Service
public class GenericPaymentCalculator implements PaymentCalculator {

    @Override
    public OrderResult process(Order order, PaymentCondition condition) {
        validate(order, condition);

        BigDecimal subtotal = calculateSubtotal(order, condition);

        BigDecimal total = applyDiscount(subtotal, condition);

        OrderResponseDTO.DiscountDTO discount = getDiscountDTO(subtotal, total, condition);

        return new OrderResult(total, condition.getAllowBonusOrder(), discount);
    }

    private void validate(Order order, PaymentCondition condition) {
        BigDecimal orderValue = order.getItems().stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (orderValue.compareTo(condition.getMinOrderValue()) < 0) {
            throw new IllegalArgumentException("Pedido abaixo do valor mínimo");
        }

        int totalItems = order.getItems().stream().mapToInt(OrderItem::getQuantity).sum();
        if (condition.getMaxItems() != null && totalItems > condition.getMaxItems()) {
            throw new IllegalArgumentException("Quantidade máxima de itens excedida");
        }

        if (Boolean.TRUE.equals(condition.getBusinessHoursOnly())) {
            LocalTime now = LocalTime.now(ZoneId.of("America/Sao_Paulo"));
            if (now.isBefore(LocalTime.of(8, 0)) || now.isAfter(LocalTime.of(18, 0))) {
                throw new IllegalArgumentException("Pedidos fora do horário comercial não são permitidos");
            }
        }
    }

    private BigDecimal calculateSubtotal(Order order, PaymentCondition condition) {
        BigDecimal subtotal = order.getItems().stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (condition.getOperationalFeePercent() != null) {
            BigDecimal fee = subtotal.multiply(condition.getOperationalFeePercent().divide(BigDecimal.valueOf(100), RoundingMode.HALF_EVEN));
            subtotal = subtotal.add(fee);
        }
        return subtotal;
    }

    private BigDecimal applyDiscount(BigDecimal subtotal, PaymentCondition condition) {
        BigDecimal discount = getDiscount(subtotal, condition);
        return subtotal.subtract(discount);
    }

    private OrderResponseDTO.DiscountDTO getDiscountDTO(BigDecimal subtotal, BigDecimal total, PaymentCondition condition) {
        OrderResponseDTO.DiscountDTO discountDTO = new OrderResponseDTO.DiscountDTO();
        discountDTO.setFreeShipping(condition.getAlwaysFreeShipping() || subtotal.compareTo(condition.getFreeShippingThreshold()) >= 0);
        discountDTO.setPercentage(condition.getDiscountPercent());
        discountDTO.setValue(subtotal.subtract(total));
        discountDTO.setDescription("Desconto padrão " + condition.getDiscountPercent() + "%");
        return discountDTO;
    }

    private BigDecimal getDiscount(BigDecimal subtotal, PaymentCondition condition) {
        BigDecimal discount = BigDecimal.ZERO;

        if (condition.getDiscountPercent() != null && condition.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0) {
            discount = discount.add(subtotal.multiply(condition.getDiscountPercent().divide(BigDecimal.valueOf(100), RoundingMode.HALF_EVEN)));
        }

        if ("Pagamento à vista".equalsIgnoreCase(condition.getDiscountCondition())) {
            discount = discount.add(subtotal.multiply(condition.getDiscountExtraPercent().divide(BigDecimal.valueOf(100), RoundingMode.HALF_EVEN)));
        }
        return discount;
    }
}