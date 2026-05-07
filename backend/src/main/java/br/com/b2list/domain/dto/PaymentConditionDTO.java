package br.com.b2list.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConditionDTO {

    private String code;
    private String description;
    private Integer maxInstallments;
    private BigDecimal discountPercentage;
    private BigDecimal extraDiscountPercentage;
    private String tenantCode;
    private BigDecimal operationalFeePercentage;
    private BigDecimal minValue;
    private Integer maxItems;
    private Boolean allowOnlyBusinessHours;
    private Boolean allowBonus;
    private BigDecimal freeShippingThreshold;
    private Boolean enabled;

}