package br.com.b2list.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderRequestDTO {

    private String externalReference;
    private String buyerReference;
    private String sellerReference;
    private String warehouseReference;
    private String paymentConditionCode;
    private List<ItemDTO> items;


}