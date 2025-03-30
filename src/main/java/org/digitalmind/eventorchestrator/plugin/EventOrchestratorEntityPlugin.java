package org.digitalmind.eventorchestrator.plugin;

import org.springframework.core.Ordered;

public interface EventOrchestratorEntityPlugin extends Ordered {


    //------------------------------------------------------------------------------------------------------------------
    // Qualify a plugin to be eligible for using in the appropriate order
    //------------------------------------------------------------------------------------------------------------------

    /**
     * Default order — override in implementation or use @Order.
     */
    @Override
    default int getOrder() {
        return Integer.MAX_VALUE;
    }

    /**
     * Determine whether this plugin supports the given entity name.
     */
    default boolean supports(String name) {
        return false;
    }

    /**
     * Resolve the entity for the given name and ID.
     */
    Object getEntity(String name, String id);

    /**
     * Return an alias for the given entity name.
     */
    String getEntityAlias(String name);

}
