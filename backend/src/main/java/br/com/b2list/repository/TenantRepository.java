package br.com.b2list.repository;

import br.com.b2list.domain.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Tenant findByCode(String code);
}
