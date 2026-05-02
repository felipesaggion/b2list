package br.com.b2list.service;

import br.com.b2list.domain.dto.TenantDTO;

import java.util.List;

public interface TenantService {

    TenantDTO save(TenantDTO tenant);

    List<TenantDTO> findAll();

    TenantDTO findByCode(String code);
}
