package br.com.b2list.service.impl;

import br.com.b2list.domain.entity.PaymentCondition;
import br.com.b2list.service.OrderDiscountStrategy;
import br.com.b2list.service.OrderPricingStrategy;
import br.com.b2list.service.OrderValidationStrategy;
import br.com.b2list.service.PaymentConditionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StrategyFactory {

    private final ApplicationContext context;
    private final PaymentConditionService paymentConditionService;

    /**
     * Resolve as configurações de banco com fallback para o FARMA-DEFAULT
     */
    public PaymentCondition resolveRules(String tenantCode, String paymentCode) {
        return paymentConditionService.findByTenantCodeAndCode(tenantCode, paymentCode)
                .or(() -> paymentConditionService.findByTenantCodeAndCode("FARMA-DEFAULT", "AVISTA"))
                .orElseThrow(() -> new RuntimeException("Nenhuma configuração de estratégia encontrada para o tenant: " + tenantCode));
    }

    /**
     * Resolve a implementação de Validação. 
     * Tenta achar "{TENANT_CODE}Validation", senão usa "DefaultValidation".
     */
    public OrderValidationStrategy getValidationStrategy(String tenantCode) {
        try {
            return context.getBean(tenantCode + "Validation", OrderValidationStrategy.class);
        } catch (Exception e) {
            return context.getBean("DefaultValidation", OrderValidationStrategy.class);
        }
    }

    /**
     * Resolve a implementação de Precificação. 
     * Tenta achar "{TENANT_CODE}Pricing", senão usa "DefaultPricing".
     */
    public OrderPricingStrategy getPricingStrategy(String tenantCode) {
        try {
            return context.getBean(tenantCode + "Pricing", OrderPricingStrategy.class);
        } catch (Exception e) {
            return context.getBean("DefaultPricing", OrderPricingStrategy.class);
        }
    }

    /**
     * Resolve a implementação de Desconto. 
     * Tenta achar "{TENANT_CODE}Discount", senão usa "DefaultDiscount".
     */
    public OrderDiscountStrategy getDiscountStrategy(String tenantCode) {
        try {
            return context.getBean(tenantCode + "Discount", OrderDiscountStrategy.class);
        } catch (Exception e) {
            return context.getBean("DefaultDiscount", OrderDiscountStrategy.class);
        }
    }
}