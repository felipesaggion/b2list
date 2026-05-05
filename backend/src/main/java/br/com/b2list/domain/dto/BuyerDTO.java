package br.com.b2list.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
public class BuyerDTO {
    private String externalReference;
    private String name;
    private BigDecimal creditLimit;
    private String tenantCode;
    private Boolean enabled;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastModified;
    private Long version;
}