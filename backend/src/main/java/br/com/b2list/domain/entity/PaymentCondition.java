package br.com.b2list.domain.entity;

import jakarta.persistence.Column;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payment_condition",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_payment_condition_tenant_code_code",
                        columnNames = {"tenant_code", "code"})
        })
public class PaymentCondition {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "tenant_code", nullable = false, length = 50)
    private String tenantCode;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(nullable = false)
    private String description;

    @Column(name = "min_order_value", precision = 15, scale = 2)
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    @Column(name = "max_items")
    private Integer maxItems;

    @Column(name = "allow_bonus_order")
    private Boolean allowBonusOrder = false;

    @Column(name = "business_hours_only")
    private Boolean businessHoursOnly = false;

    @Column(name = "operational_fee_percent", precision = 5, scale = 2)
    private BigDecimal operationalFeePercent = BigDecimal.ZERO;

    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(name = "discount_extra_percent", precision = 5, scale = 2)
    private BigDecimal discountExtraPercent = BigDecimal.ZERO;

    @Column(name = "discount_condition", length = 255)
    private String discountCondition;

    @Column(name = "free_shipping_threshold", precision = 15, scale = 2)
    private BigDecimal freeShippingThreshold = BigDecimal.ZERO;

    @Column(name = "always_free_shipping")
    private Boolean alwaysFreeShipping = false;

    @Column(name = "enabled")
    private Boolean enabled = true;

    public PaymentCondition() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getMinOrderValue() {
        return minOrderValue;
    }

    public void setMinOrderValue(BigDecimal minOrderValue) {
        this.minOrderValue = minOrderValue;
    }

    public Integer getMaxItems() {
        return maxItems;
    }

    public void setMaxItems(Integer maxItems) {
        this.maxItems = maxItems;
    }

    public Boolean getAllowBonusOrder() {
        return allowBonusOrder;
    }

    public void setAllowBonusOrder(Boolean allowBonusOrder) {
        this.allowBonusOrder = allowBonusOrder;
    }

    public Boolean getBusinessHoursOnly() {
        return businessHoursOnly;
    }

    public void setBusinessHoursOnly(Boolean businessHoursOnly) {
        this.businessHoursOnly = businessHoursOnly;
    }

    public BigDecimal getOperationalFeePercent() {
        return operationalFeePercent;
    }

    public void setOperationalFeePercent(BigDecimal operationalFeePercent) {
        this.operationalFeePercent = operationalFeePercent;
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(BigDecimal discountPercent) {
        this.discountPercent = discountPercent;
    }

    public BigDecimal getDiscountExtraPercent() {
        return discountExtraPercent;
    }

    public void setDiscountExtraPercent(BigDecimal discountExtraPercent) {
        this.discountExtraPercent = discountExtraPercent;
    }

    public String getDiscountCondition() {
        return discountCondition;
    }

    public void setDiscountCondition(String discountCondition) {
        this.discountCondition = discountCondition;
    }

    public BigDecimal getFreeShippingThreshold() {
        return freeShippingThreshold;
    }

    public void setFreeShippingThreshold(BigDecimal freeShippingThreshold) {
        this.freeShippingThreshold = freeShippingThreshold;
    }

    public Boolean getAlwaysFreeShipping() {
        return alwaysFreeShipping;
    }

    public void setAlwaysFreeShipping(Boolean alwaysFreeShipping) {
        this.alwaysFreeShipping = alwaysFreeShipping;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}