package org.digitalmind.eventorchestrator.converter.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Configuration
public class JpaGenericConverterProperties {

    private static JpaGenericConverterProperties config = null;


    @Value("${application.config.resource.secrets.db-key:db-key}")
    private String key;

    @Value("${application.config.resource.secrets.db-key-algorithm:AES}")
    private String algorithm;

    public static JpaGenericConverterProperties getConfig() {
        return config;
    }

    @PostConstruct
    public void init() {
        config = this;
    }

}
