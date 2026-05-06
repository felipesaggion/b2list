package br.com.b2list.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
public class ErrorResponseDTO {

    private int status;
    private String code;
    private String message;
    private List<String> details;
    private String traceId;
    private OffsetDateTime timestamp;


}