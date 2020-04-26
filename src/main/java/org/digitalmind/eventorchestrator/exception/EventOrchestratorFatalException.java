package org.digitalmind.eventorchestrator.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Event Orchestrator Fatal Exception")
public class EventOrchestratorFatalException extends EventOrchestratorException {

    public EventOrchestratorFatalException() {
    }

    public EventOrchestratorFatalException(String message) {
        super(message);
    }

    public EventOrchestratorFatalException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventOrchestratorFatalException(Throwable cause) {
        super(cause);
    }

    public EventOrchestratorFatalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
