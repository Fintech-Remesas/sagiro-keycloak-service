package com.sagiro.iamservice.infrastructure.config;

import com.sagiro.iamservice.infrastructure.keycloak.client.KeycloakAdminProperties;
import com.sagiro.iamservice.infrastructure.messaging.kafka.config.KafkaTopicsProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({KeycloakAdminProperties.class, KafkaTopicsProperties.class})
public class AppPropertiesConfig {
}
