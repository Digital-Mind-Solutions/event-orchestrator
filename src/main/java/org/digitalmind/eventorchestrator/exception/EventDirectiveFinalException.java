package org.digitalmind.eventorchestrator.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Event Directive Final Exception")
public class EventDirectiveFinalException extends EventDirectiveException {

    public EventDirectiveFinalException() {
    }

    public EventDirectiveFinalException(String message) {
        super(message);
    }

    public EventDirectiveFinalException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventDirectiveFinalException(Throwable cause) {
        super(cause);
    }

    public EventDirectiveFinalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
