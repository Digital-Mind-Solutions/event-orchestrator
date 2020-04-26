package org.digitalmind.eventorchestrator.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Event Orchestrator Retry Exception")
public class EventOrchestratorRetryException extends EventOrchestratorException {

    public EventOrchestratorRetryException() {
    }

    public EventOrchestratorRetryException(String message) {
        super(message);
    }

    public EventOrchestratorRetryException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventOrchestratorRetryException(Throwable cause) {
        super(cause);
    }

    public EventOrchestratorRetryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
