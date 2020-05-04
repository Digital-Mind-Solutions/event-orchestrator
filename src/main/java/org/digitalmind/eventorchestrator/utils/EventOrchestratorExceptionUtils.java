package org.digitalmind.eventorchestrator.utils;

import org.digitalmind.eventorchestrator.exception.EventDirectiveException;
import org.digitalmind.eventorchestrator.exception.EventOrchestratorException;
import org.springframework.expression.ExpressionInvocationTargetException;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public class EventOrchestratorExceptionUtils {

    public static Throwable getExceptionCause(Exception e) {
        Throwable cause = e;
        if (cause.getCause() != null) {
            cause = e.getCause();
        }
        if (cause instanceof EventDirectiveException) {
            cause = cause.getCause();
        }
        if (cause instanceof EventOrchestratorException) {
            cause = cause.getCause();
        }
        if (cause instanceof ExpressionInvocationTargetException) {
            cause = cause.getCause();
        }
        if (cause instanceof ExecutionException) {
            cause = cause.getCause();
        }
        if (cause instanceof CompletionException) {
            cause = cause.getCause();
        }
        return cause;
    }

}
