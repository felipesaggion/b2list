package br.com.b2list.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
public class PeriodDTO {
    private OffsetDateTime from;
    private OffsetDateTime to;
}