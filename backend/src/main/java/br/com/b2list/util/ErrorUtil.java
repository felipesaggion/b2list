package br.com.b2list.util;

import br.com.b2list.domain.dto.ErrorResponseDTO;
import br.com.b2list.enums.Error;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ErrorUtil {
    public static ErrorResponseDTO buildErrorResponse(Error error) {
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO();
        errorResponseDTO.setStatus(error.getHttpStatus().value());
        errorResponseDTO.setCode(error.getErrorCode());
        errorResponseDTO.setMessage(error.getMessage());
        errorResponseDTO.setTraceId(UUID.randomUUID().toString());
        errorResponseDTO.setTimestamp(OffsetDateTime.now());
        return errorResponseDTO;
    }
}
