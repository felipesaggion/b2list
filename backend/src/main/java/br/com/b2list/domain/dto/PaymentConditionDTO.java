package br.com.b2list.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentConditionDTO {
    private String tenantCode;
    private String code;
    private String description;
    private BigDecimal minOrderValue;
    private Integer maxItems;
    private Boolean allowBonusOrder;
    private Boolean businessHoursOnly;
    private BigDecimal operationalFeePercent;
    private BigDecimal discountPercent;
    private BigDecimal discountExtraPercent;
    private String discountCondition;
    private BigDecimal freeShippingThreshold;
    private Boolean alwaysFreeShipping;
    private Boolean enabled;
}