package com.umdc.backoffice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfisicalBootstrapConfiguration {

    @Bean
    public InfisicalPropertySourceLocator infisicalPropertySourceLocator() {
        return new InfisicalPropertySourceLocator();
    }
}
