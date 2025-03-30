package org.digitalmind.eventorchestrator.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static org.digitalmind.eventorchestrator.config.EventOrchestratorModuleConfig.*;

@Configuration
@ComponentScan({
        LISTENER_PACKAGE,
        PLUGIN_PACKAGE,
        CONVERTER_PACKAGE,
        SERVICE_PACKAGE,
        MAPPER_PACKAGE,
        API_PACKAGE
})
@ConditionalOnProperty(name = EventOrchestratorModuleConfig.ENABLED, havingValue = "true")
public class EventOrchestratorModuleConfig {

    public static final String MODULE = "eventorchestrator";
    public static final String PREFIX = "application.modules.common." + MODULE;
    public static final String ENABLED = PREFIX + ".enabled";
    public static final String API_ENABLED = PREFIX + ".api.enabled";

    public static final String ROOT_PACKAGE = "org.digitalmind." + MODULE;
    public static final String CONFIG_PACKAGE = ROOT_PACKAGE + ".config";
    public static final String ENTITY_PACKAGE = ROOT_PACKAGE + ".entity";
    public static final String REPOSITORY_PACKAGE = ROOT_PACKAGE + ".repository";
    public static final String SERVICE_PACKAGE = ROOT_PACKAGE + ".service";
    public static final String MAPPER_PACKAGE = ROOT_PACKAGE + ".mapper";
    public static final String API_PACKAGE = ROOT_PACKAGE + ".api";
    public static final String LISTENER_PACKAGE = ROOT_PACKAGE + ".listener";
    public static final String CONVERTER_PACKAGE = ROOT_PACKAGE + ".converter";
    public static final String PLUGIN_PACKAGE = ROOT_PACKAGE + ".plugin";

    public static final String CACHE_NAME = MODULE + "-cache";

    public static final String EVENT_ORCHESTRATOR_PLUGIN_REGISTRY = "eventOrchestratorPluginRegistry";

}

