package br.com.b2list.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductPriceDTO {
    private UUID id;
    private String productCode;
    private String productName;
    private UUID warehouseId;
    private BigDecimal unitPrice;
    private BigDecimal listPrice;
    private String tenantCode;
    private Boolean enabled;
    private OffsetDateTime lastModified;
}
