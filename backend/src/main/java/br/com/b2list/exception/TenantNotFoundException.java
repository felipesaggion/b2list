package br.com.b2list.exception;

import br.com.b2list.enums.Error;
import lombok.Getter;

@Getter
public class TenantNotFoundException extends Exception {
    private final Error error;

    public TenantNotFoundException(Error error) {
        super(error.getMessage());
        this.error = error;
    }

    public TenantNotFoundException(String message, Error error) {
        super(message);
        this.error = error;
    }
}
