package br.com.b2list.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReprocessParkingLotRequest {
    private String queue;
    private int maxMessages;
}