package br.com.b2list.enums;

import org.springframework.http.HttpStatus;

public enum Error {
    ORD_VALIDATION_001("ORD-VALIDATION-001", HttpStatus.UNPROCESSABLE_ENTITY, "Campo obrigatório ausente"),
    ORD_VALIDATION_002("ORD-VALIDATION-002", HttpStatus.UNPROCESSABLE_ENTITY, "Entidade referenciada não encontrada"),
    ORD_VALIDATION_003("ORD-VALIDATION-003", HttpStatus.UNPROCESSABLE_ENTITY, "Limite de crédito insuficiente"),
    ORD_VALIDATION_004("ORD-VALIDATION-004", HttpStatus.UNPROCESSABLE_ENTITY, "Produto não encontrado na tabela de preços"),
    ORD_VALIDATION_005("ORD-VALIDATION-005", HttpStatus.UNPROCESSABLE_ENTITY, "Falha na validação da strategy do tenant"),
    ORD_VALIDATION_006("ORD-VALIDATION-006", HttpStatus.BAD_REQUEST, "Campos obrigatorios estão faltando"),
    ORD_VALIDATION_007("ORD-VALIDATION-007", HttpStatus.UNPROCESSABLE_ENTITY, "Falha na validação das regras de negocio"),
    ORD_VALIDATION_008("ORD-VALIDATION-008", HttpStatus.UNPROCESSABLE_ENTITY, "Limite de tamanho para pagina excedido, max 50."),
    ORD_DUPLICATE_001("ORD-DUPLICATE-001", HttpStatus.CONFLICT, "External reference duplicado"),
    ORD_STATUS_001("ORD-STATUS-001", HttpStatus.UNPROCESSABLE_ENTITY, "Transição de status inválida"),
    ORD_CONCURRENCY_001("ORD-CONCURRENCY-001", HttpStatus.CONFLICT, "Conflito de concorrência (optimistic lock)");

    private final String errorCode;
    private final HttpStatus httpStatus;
    private final String message;

    Error(String errorCode, HttpStatus httpErrorCode, String message) {
        this.errorCode = errorCode;
        this.httpStatus = httpErrorCode;
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }
}
