package org.digitalmind.eventorchestrator.dto.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class MapperBadRequestException extends RuntimeException {

    public MapperBadRequestException() {
    }

    public MapperBadRequestException(String message) {
        super(message);
    }

    public MapperBadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public MapperBadRequestException(Throwable cause) {
        super(cause);
    }

    public MapperBadRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
