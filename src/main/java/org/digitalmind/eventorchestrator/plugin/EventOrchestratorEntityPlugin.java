package org.digitalmind.eventorchestrator.plugin;

import org.springframework.core.Ordered;
import org.springframework.plugin.core.Plugin;

public interface EventOrchestratorEntityPlugin extends Plugin<String>, Ordered {


    //------------------------------------------------------------------------------------------------------------------
    // Qualify a plugin to be eligible for using in the appropriate order
    //------------------------------------------------------------------------------------------------------------------

    @Override
    default int getOrder() {
        return Integer.MAX_VALUE;
    }

    @Override
    default boolean supports(String name) {
        return false;
    }

    Object getEntity(String name, String id);

    String getEntityAlias(String name);

}
