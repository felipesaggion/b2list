package br.com.b2list.repository;

import br.com.b2list.domain.entity.PaymentCondition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentConditionRepository extends JpaRepository<PaymentCondition, UUID> {
    PaymentCondition findByCodeAndEnabledTrueAndTenantCode(String code, String tenantCode);
    Optional<PaymentCondition> findByTenantCodeAndCode(String tenantCode, String code);
}
