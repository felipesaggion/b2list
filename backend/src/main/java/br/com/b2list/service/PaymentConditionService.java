package br.com.b2list.service;

import br.com.b2list.domain.dto.PaymentConditionDTO;
import br.com.b2list.domain.entity.PaymentCondition;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentConditionService {
    PaymentConditionDTO save(PaymentConditionDTO buyer);

    List<PaymentConditionDTO> findByTenantCodeAndEnabledTrue(String tenantCode);

    PaymentConditionDTO findById(UUID id);

    Optional<PaymentCondition> findByTenantCodeAndCode(String tenantCode, String code);

    PaymentCondition findByCodeAndEnabledTrueAndTenantCode(String externalReference, String tenantCode);

    void deleteById(UUID id);

}
