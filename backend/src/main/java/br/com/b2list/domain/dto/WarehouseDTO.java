package br.com.b2list.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class WarehouseDTO {
    private String externalReference;
    private String name;
    private String tenantCode;
    private Boolean enabled;
    private UUID sellerId;
    private OffsetDateTime createdAt;
}