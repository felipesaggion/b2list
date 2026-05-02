package br.com.b2list.service.impl;

import br.com.b2list.domain.dto.TenantDTO;
import br.com.b2list.domain.entity.Tenant;
import br.com.b2list.repository.TenantRepository;
import br.com.b2list.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class TenantServiceImpl implements TenantService {

    @Autowired
    private TenantRepository tenantRepository;

    @Override
    public TenantDTO save(TenantDTO tenantDTO) {
        Tenant tenantEntity = new Tenant();
        tenantEntity.setCode(tenantDTO.getCode());
        tenantEntity.setName(tenantDTO.getName());
        tenantEntity.setEnabled(tenantDTO.isEnabled());

        if (tenantEntity.getId() == null) {
            tenantEntity.setCreatedAt(OffsetDateTime.now());
        }

        Tenant tenantSaved = tenantRepository.save(tenantEntity);

        TenantDTO dto = new TenantDTO();
        dto.setCode(tenantSaved.getCode());
        dto.setName(tenantSaved.getName());
        dto.setEnabled(tenantSaved.isEnabled());
        return dto;
    }

    @Override
    public List<TenantDTO> findAll() {
        return tenantRepository.findAll().stream().map(tenant -> {
            TenantDTO dto = new TenantDTO();
            dto.setCode(tenant.getCode());
            dto.setName(tenant.getName());
            dto.setEnabled(tenant.isEnabled());
            return dto;
        }).toList();
    }

    @Override
    public TenantDTO findByCode(String code) {
        Tenant tenantEntity = tenantRepository.findByCode(code);

        TenantDTO dto = new TenantDTO();
        dto.setCode(tenantEntity.getCode());
        dto.setName(tenantEntity.getName());
        dto.setEnabled(tenantEntity.isEnabled());
        return dto;
    }
}
