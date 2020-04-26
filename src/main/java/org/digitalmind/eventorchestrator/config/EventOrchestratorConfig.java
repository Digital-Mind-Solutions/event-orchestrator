package org.digitalmind.eventorchestrator.config;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@ConditionalOnProperty(name = EventOrchestratorModuleConfig.ENABLED, havingValue = "true")
@ConfigurationProperties(prefix = EventOrchestratorModuleConfig.PREFIX)
@EnableConfigurationProperties
@Data
public class EventOrchestratorConfig {
    private EventOrchestratorCaches cache;
    private EventOrchestratorPolls threadPoolExecutor;

    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class EventOrchestratorCaches {
        private String templateFlow;
        private String templateActivity;
        private String templateActivityActivator;
    }

    @Data
    public static class EventOrchestratorPolls {
        private EventOrchestratorPollConfiguration parallel;
        private EventOrchestratorPollConfiguration serialProcess;
        private EventOrchestratorPollConfiguration serialEntity;
        private EventOrchestratorPollConfiguration heartbeat;
        private EventOrchestratorPollConfiguration orphan;

    }

    @Data
    public static class EventOrchestratorPollConfiguration {
        private String threadPoolName;
        private long schedulerInitDelay;
        private long schedulerPeriod;
        private TimeUnit schedulerUnit;
        private int schedulerCapacityThreshold;
    }
}
