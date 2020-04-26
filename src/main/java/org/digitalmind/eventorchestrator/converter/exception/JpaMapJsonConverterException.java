package org.digitalmind.eventorchestrator.converter.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
public class JpaMapJsonConverterException extends RuntimeException {

    public JpaMapJsonConverterException() {
    }

    public JpaMapJsonConverterException(String message) {
        super(message);
    }

    public JpaMapJsonConverterException(String message, Throwable cause) {
        super(message, cause);
    }

    public JpaMapJsonConverterException(Throwable cause) {
        super(cause);
    }

    public JpaMapJsonConverterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
