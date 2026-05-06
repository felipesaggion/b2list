package br.com.b2list.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReprocessParkingLotRequest {
    private String queue;
    private int maxMessages;
}