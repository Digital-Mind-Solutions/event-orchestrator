package org.digitalmind.eventorchestrator.enumeration;

public enum ExceptionType {
    RETRY,
    FINAL,
    FATAL;

    public boolean isRetry() {
        return RETRY.equals(this);
    }

    public boolean isFinal() {
        return FINAL.equals(this);
    }

    public boolean isFatal() {
        return FATAL.equals(this);
    }

}
