package org.digitalmind.eventorchestrator.config;

import org.digitalmind.eventorchestrator.plugin.EventOrchestratorEntityPlugin;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.digitalmind.eventorchestrator.config.EventOrchestratorModuleConfig.EVENT_ORCHESTRATOR_PLUGIN_REGISTRY;

@Component(EVENT_ORCHESTRATOR_PLUGIN_REGISTRY)
@ConditionalOnProperty(name = EventOrchestratorModuleConfig.ENABLED, havingValue = "true")
public class EventOrchestratorPluginRegistry {

    private final List<EventOrchestratorEntityPlugin> plugins;

    public EventOrchestratorPluginRegistry(List<EventOrchestratorEntityPlugin> plugins) {
        // Spring injects all beans of type EventOrchestratorEntityPlugin, already ordered
        this.plugins = plugins;
    }

    public Optional<EventOrchestratorEntityPlugin> getPluginFor(String type) {
        return plugins.stream()
                .filter(p -> p.supports(type))
                .findFirst();
    }

    public List<EventOrchestratorEntityPlugin> getPluginsFor(String type) {
        return plugins.stream()
                .filter(p -> p.supports(type))
                .collect(Collectors.toList());
    }

    public List<EventOrchestratorEntityPlugin> getAll() {
        return plugins;
    }

    public List<EventOrchestratorEntityPlugin> getAllSupporting(String name) {
        return plugins.stream()
                .filter(p -> p.supports(name))
                .collect(Collectors.toList());
    }


}
