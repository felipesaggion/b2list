package br.com.b2list.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantDTO {
    private String code;
    private String name;
    private boolean enabled;
}