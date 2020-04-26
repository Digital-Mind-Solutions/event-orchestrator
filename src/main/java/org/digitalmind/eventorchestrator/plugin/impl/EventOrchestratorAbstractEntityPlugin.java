package org.digitalmind.eventorchestrator.plugin.impl;


import org.digitalmind.eventorchestrator.exception.EventOrchestratorFatalException;
import org.digitalmind.eventorchestrator.plugin.EventOrchestratorEntityPlugin;

public abstract class EventOrchestratorAbstractEntityPlugin implements EventOrchestratorEntityPlugin {

    @Override
    public int getOrder() {
        return 0;
    }

    protected abstract boolean supportsInternal(String name);

    @Override
    public final boolean supports(String name) {
        if (name == null) {
            return false;
        }
        return supportsInternal(name);
    }

    protected abstract Object getEntityInternal(String name, String id);

    @Override
    public final Object getEntity(String name, String id) {
        if (!supports(name)) {
            throw new EventOrchestratorFatalException("The <" + this.getClass().getSimpleName() + "> does not support entity type <" + String.valueOf(name) + ">");
        }
        if (id == null) {
            return null;
        }
        return getEntityInternal(name, id);
    }

    @Override
    public String getEntityAlias(String name) {
        return name;
    }

    protected String getEntityAliasAsSimpleName(String name) {
        if (supports(name)) {
            int i = name.lastIndexOf(".");
            if (i >= 0) {
                return name.substring(i + 1);
            }
        }
        return null;
    }

}
