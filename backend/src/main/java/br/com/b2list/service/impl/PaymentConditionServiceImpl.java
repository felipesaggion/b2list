package br.com.b2list.service.impl;

import br.com.b2list.domain.dto.PaymentConditionDTO;
import br.com.b2list.domain.entity.PaymentCondition;
import br.com.b2list.mapper.PaymentConditionMapper;
import br.com.b2list.repository.PaymentConditionRepository;
import br.com.b2list.service.PaymentConditionService;
import br.com.b2list.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentConditionServiceImpl implements PaymentConditionService {
    @Autowired
    private PaymentConditionRepository paymentConditionRepository;

    @Autowired
    private PaymentConditionMapper paymentConditionMapper;

    @Override
    public PaymentConditionDTO save(PaymentConditionDTO paymentConditionDTO) {
        paymentConditionDTO.setTenantCode(TenantContext.getTenant());
        PaymentCondition paymentCondition = paymentConditionRepository.findByCodeAndEnabledTrueAndTenantCode(
                paymentConditionDTO.getCode(),
                paymentConditionDTO.getTenantCode()
        );
        UUID id = paymentCondition.getId();
        paymentCondition = paymentConditionMapper.toEntity(paymentConditionDTO);
        paymentCondition.setId(id);
        return paymentConditionMapper.toDto(paymentConditionRepository.save(paymentCondition));
    }

    @Override
    public List<PaymentConditionDTO> findByTenantCodeAndEnabledTrue(String tenant) {
        return paymentConditionRepository.findByTenantCodeAndEnabledTrue(tenant)
                .stream().map(paymentConditionMapper::toDto).toList();
    }

    @Override
    public PaymentConditionDTO findById(UUID id) {
        return paymentConditionMapper.toDto(paymentConditionRepository.findById(id).orElse(null));
    }

    @Override
    public Optional<PaymentCondition> findByTenantCodeAndCode(String tenantCode, String code) {
        return paymentConditionRepository.findByTenantCodeAndCode(tenantCode, code);
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
