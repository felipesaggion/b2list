package br.com.b2list.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class WarehouseDTO {
    private String externalReference;
    private String name;
    private String tenantCode;
    private Boolean enabled;
    private OffsetDateTime createdAt;
}