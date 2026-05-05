package br.com.b2list.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class TopBuyerDTO {
    private String name;
    private Long orderCount;
    private BigDecimal totalSpent;
}