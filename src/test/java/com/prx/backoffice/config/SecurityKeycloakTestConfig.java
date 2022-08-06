package com.prx.backoffice.config;

import com.prx.backoffice.config.security.keycloak.CustomKeycloakSpringBootConfigResolver;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import( CustomKeycloakSpringBootConfigResolver.class )
public interface SecurityKeycloakTestConfig {

}
