package br.com.b2list.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TopProductDTO {
    private String productCode;
    private String productName;
    private Long totalQuantity;
}