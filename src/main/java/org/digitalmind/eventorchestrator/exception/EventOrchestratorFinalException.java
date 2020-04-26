package org.digitalmind.eventorchestrator.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Event Orchestrator Final Exception")
public class EventOrchestratorFinalException extends EventOrchestratorException {

    public EventOrchestratorFinalException() {
    }

    public EventOrchestratorFinalException(String message) {
        super(message);
    }

    public EventOrchestratorFinalException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventOrchestratorFinalException(Throwable cause) {
        super(cause);
    }

    public EventOrchestratorFinalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
