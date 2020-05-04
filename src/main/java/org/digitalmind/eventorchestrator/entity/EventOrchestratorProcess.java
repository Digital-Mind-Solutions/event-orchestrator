package org.digitalmind.eventorchestrator.entity;

public interface EventOrchestratorProcess {

    String getFlowTemplate();

    Long getId();

    boolean setFatalCause(Throwable throwable);
}
