package org.digitalmind.eventorchestrator.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Event Directive Exception")
public abstract class EventDirectiveException extends RuntimeException {

    public EventDirectiveException() {
        super();
    }

    public EventDirectiveException(String message) {
        super(message);
    }

    public EventDirectiveException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventDirectiveException(Throwable cause) {
        super(cause);
    }

    protected EventDirectiveException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
