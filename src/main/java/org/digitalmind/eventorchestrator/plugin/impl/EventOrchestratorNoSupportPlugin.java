package org.digitalmind.eventorchestrator.plugin.impl;

import org.digitalmind.eventorchestrator.exception.EventOrchestratorFatalException;
import org.springframework.stereotype.Component;

@Component
public class EventOrchestratorNoSupportPlugin extends EventOrchestratorAbstractEntityPlugin {

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected boolean supportsInternal(String name) {
        return true;
    }

    @Override
    protected Object getEntityInternal(String name, String id) {
        throw new EventOrchestratorFatalException("Unable to find event orchestrator plugin to load entity of type <" + name + ">");
    }

}
