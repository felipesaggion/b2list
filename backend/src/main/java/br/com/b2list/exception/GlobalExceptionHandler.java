package br.com.b2list.exception;

import br.com.b2list.domain.dto.ErrorResponseDTO;
import br.com.b2list.enums.Error;
import br.com.b2list.util.ErrorUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataIntegrity(DataIntegrityViolationException ex) {
        ErrorResponseDTO error = ErrorUtil.buildErrorResponse(Error.ORD_DUPLICATE_001);
        List<String> details = List.of(
                "External reference duplicado"
        );
        error.setDetails(details);
        log.error(ToStringBuilder.reflectionToString(error, ToStringStyle.MULTI_LINE_STYLE));
        return ResponseEntity.status(Error.ORD_DUPLICATE_001.getHttpStatus()).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponseDTO error = ErrorUtil.buildErrorResponse(Error.ORD_VALIDATION_007);
        List<String> details = List.of(ex.getMessage());
        error.setDetails(details);
        log.error(ToStringBuilder.reflectionToString(error, ToStringStyle.MULTI_LINE_STYLE));
        return ResponseEntity.unprocessableEntity().body(error);
    }
}