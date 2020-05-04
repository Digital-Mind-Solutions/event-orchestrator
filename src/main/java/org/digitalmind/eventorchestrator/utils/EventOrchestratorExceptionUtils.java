package org.digitalmind.eventorchestrator.utils;

import org.digitalmind.eventorchestrator.enumeration.ExceptionType;
import org.digitalmind.eventorchestrator.exception.*;
import org.springframework.expression.ExpressionException;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public class EventOrchestratorExceptionUtils {

    public static Throwable getExceptionCause(Throwable throwable) {
        if (
                (
                        throwable instanceof EventDirectiveException
                                ||
                                throwable instanceof EventOrchestratorException
                                ||
                                throwable instanceof ExpressionException
                                ||
                                throwable instanceof ExecutionException
                                ||
                                throwable instanceof CompletionException
                )
                        &&
                        throwable.getCause() != null) {
            return getExceptionCause(throwable.getCause());
        }
        if (throwable.getCause() != null) {
            return throwable.getCause();
        }
        return throwable;
    }

    public static ExceptionType getExceptionType(Throwable throwable) {
        if(throwable instanceof EventDirectiveFinalException){
            return ExceptionType.FINAL;
        }

        if(throwable instanceof EventOrchestratorRetryException){
            return ExceptionType.RETRY;
        }

        if(throwable instanceof EventOrchestratorFinalException){
            return ExceptionType.FINAL;
        }

        if(throwable instanceof EventOrchestratorFatalException){
            return ExceptionType.FATAL;
        }

        if(throwable instanceof ExpressionException){
            return ExceptionType.FATAL;
        }

        if(throwable instanceof ExecutionException){
            return ExceptionType.FATAL;
        }

        return ExceptionType.RETRY;
    }

}
