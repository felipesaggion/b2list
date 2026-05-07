package br.com.b2list.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
public class PaymentCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(name = "max_installments")
    private Integer maxInstallments;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(name = "extra_discount_percentage", precision = 5, scale = 2)
    private BigDecimal extraDiscountPercentage;

    @Column(name = "tenant_code", nullable = false, length = 50)
    private String tenantCode;

    @Column(name = "operational_fee_percentage", precision = 5, scale = 2)
    private BigDecimal operationalFeePercentage;

    @Column(name = "min_value", precision = 15, scale = 2)
    private BigDecimal minValue;

    @Column(name = "max_items")
    private Integer maxItems;

    @Column(name = "allow_only_business_hours")
    private Boolean allowOnlyBusinessHours;

    @Column(name = "allow_bonus")
    private Boolean allowBonus;

    @Column(name = "free_shipping_threshold", precision = 15, scale = 2)
    private BigDecimal freeShippingThreshold;

    @Column(nullable = false)
    private Boolean enabled;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public Integer getMaxInstallments() {
        return maxInstallments;
    }

    public void setMaxInstallments(Integer maxInstallments) {
        this.maxInstallments = maxInstallments;
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public BigDecimal getExtraDiscountPercentage() {
        return extraDiscountPercentage;
    }

    public void setExtraDiscountPercentage(BigDecimal extraDiscountPercentage) {
        this.extraDiscountPercentage = extraDiscountPercentage;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public BigDecimal getOperationalFeePercentage() {
        return operationalFeePercentage;
    }

    public void setOperationalFeePercentage(BigDecimal operationalFeePercentage) {
        this.operationalFeePercentage = operationalFeePercentage;
    }

    public BigDecimal getMinValue() {
        return minValue;
    }

    public void setMinValue(BigDecimal minValue) {
        this.minValue = minValue;
    }

    public Integer getMaxItems() {
        return maxItems;
    }

    public void setMaxItems(Integer maxItems) {
        this.maxItems = maxItems;
    }

    public Boolean getAllowOnlyBusinessHours() {
        return allowOnlyBusinessHours;
    }

    public void setAllowOnlyBusinessHours(Boolean allowOnlyBusinessHours) {
        this.allowOnlyBusinessHours = allowOnlyBusinessHours;
    }

    public Boolean getAllowBonus() {
        return allowBonus;
    }

    public void setAllowBonus(Boolean allowBonus) {
        this.allowBonus = allowBonus;
    }

    public BigDecimal getFreeShippingThreshold() {
        return freeShippingThreshold;
    }

    public void setFreeShippingThreshold(BigDecimal freeShippingThreshold) {
        this.freeShippingThreshold = freeShippingThreshold;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}