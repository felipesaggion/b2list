package br.com.b2list.controller;


import br.com.b2list.domain.dto.TenantDTO;
import br.com.b2list.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tenant")
public class TenantController {

    @Autowired
    private TenantService tenantService;

    @GetMapping
    public List<TenantDTO> findAll() {
        return tenantService.findAll();
    }

    @GetMapping("/{code}")
    public TenantDTO findByCode(@PathVariable String code) {
        return tenantService.findByCode(code);
    }

    @PostMapping
    public TenantDTO save(@RequestBody TenantDTO tenantDTO) {
        return tenantService.save(tenantDTO);
    }
}
