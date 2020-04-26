package org.digitalmind.eventorchestrator.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Event Orchestrator Exception")
public abstract class EventOrchestratorException extends RuntimeException {

    public EventOrchestratorException() {
        super();
    }

    public EventOrchestratorException(String message) {
        super(message);
    }

    public EventOrchestratorException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventOrchestratorException(Throwable cause) {
        super(cause);
    }

    protected EventOrchestratorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
