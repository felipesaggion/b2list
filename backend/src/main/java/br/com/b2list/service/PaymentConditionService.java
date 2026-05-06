package br.com.b2list.service;

import br.com.b2list.domain.dto.PaymentConditionDTO;
import br.com.b2list.domain.entity.PaymentCondition;

import java.util.List;
import java.util.UUID;

public interface PaymentConditionService {
    PaymentConditionDTO save(PaymentConditionDTO buyer);

    List<PaymentConditionDTO> findAll();

    PaymentConditionDTO findById(UUID id);

    PaymentCondition findByCodeAndEnabledTrueAndTenantCode(String externalReference, String tenantCode);

    void deleteById(UUID id);

}
