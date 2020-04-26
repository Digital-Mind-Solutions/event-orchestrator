package org.digitalmind.eventorchestrator.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class EventDirectiveConfig {
    @Value("${application.configuration-directives.cache.specification}")
    private String cacheBuilderSpecification;
}
