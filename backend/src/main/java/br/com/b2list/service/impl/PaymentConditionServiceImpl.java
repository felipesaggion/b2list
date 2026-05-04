package br.com.b2list.service.impl;

import br.com.b2list.domain.entity.PaymentCondition;
import br.com.b2list.repository.PaymentConditionRepository;
import br.com.b2list.service.PaymentConditionService;
import br.com.b2list.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentConditionServiceImpl implements PaymentConditionService {
    @Autowired
    private PaymentConditionRepository paymentConditionRepository;

    @Override
    public PaymentCondition save(PaymentCondition paymentCondition) {
        paymentCondition.setTenantCode(TenantContext.getTenant());
        return paymentConditionRepository.save(paymentCondition);
    }

    @Override
    public List<PaymentCondition> findAll() {
        return paymentConditionRepository.findAll();
    }

    @Override
    public PaymentCondition findById(UUID id) {
        return paymentConditionRepository.findById(id).orElse(null);
    }

    @Override
    public PaymentCondition findByCodeAndEnabledTrueAndTenantCode(String code, String tenantCode) {
        return paymentConditionRepository.findByCodeAndEnabledTrueAndTenantCode(code, tenantCode);
    }

    @Override
    public void deleteById(UUID id) {
        paymentConditionRepository.deleteById(id);
    }
}
