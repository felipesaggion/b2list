package br.com.b2list.domain.dto;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class OrderResponseDTO {

    private String code;
    private String message;
    private DataDTO data;

    @Getter
    @Setter
    public static class DataDTO {
        private String orderId;
        private String externalReference;
        private String status;
        private BigDecimal subtotal;
        private BigDecimal discountValue;
        private BigDecimal total;
        private Integer itemCount;
        private ValidationDTO validation;
        private PricingDTO pricing;
        private DiscountDTO discount;
    }

    @Getter
    @Setter
    public static class ValidationDTO {
        private List<String> warnings;
    }

    @Getter
    @Setter
    public static class PricingDTO {
        private BigDecimal subtotal;
        private String description;
    }

    @Getter
    @Setter
    public static class DiscountDTO {
        private BigDecimal value;
        private BigDecimal percentage;
        private String description;
        private boolean freeShipping;
    }
}