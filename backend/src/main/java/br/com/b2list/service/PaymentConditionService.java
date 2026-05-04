package br.com.b2list.service;

import br.com.b2list.domain.entity.PaymentCondition;

import java.util.List;
import java.util.UUID;

public interface PaymentConditionService {
    PaymentCondition save(PaymentCondition buyer);
    List<PaymentCondition> findAll();
    PaymentCondition findById(UUID id);
    PaymentCondition findByCodeAndEnabledTrueAndTenantCode(String externalReference, String tenantCode);
    void deleteById(UUID id);

}
