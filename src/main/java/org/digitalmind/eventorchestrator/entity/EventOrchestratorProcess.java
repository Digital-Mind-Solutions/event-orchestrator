package org.digitalmind.eventorchestrator.entity;

public interface EventOrchestratorProcess {

    String getFlowTemplate();

    Long getId();

    boolean setFatalException(Exception e);
}
