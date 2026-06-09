/*
 *  @(#)SecurityBeansConfig.java
 *
 *  Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *
 *   All rights to this product are owned by Luis Antonio Mata Mata and may only
 *  be used under the terms of its associated license document. You may NOT
 *  copy, modify, sublicense, or distribute this source file or portions of
 *  it unless previously authorized in writing by Luis Antonio Mata Mata.
 *  In any event, this notice and the above copyright must always be included
 *  verbatim with this file.
 */
package com.umdc.backoffice.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
/**
 * Security beans configuration providing password encoding infrastructure.
 * Declared outside the security package to avoid conflicts with commented-out
 * spring-boot-starter-security auto-configuration.
 */
@Configuration
public class SecurityBeansConfig {
    /**
     * Creates a SecurityBeansConfig instance.
     */
    public SecurityBeansConfig() {
        // Default constructor required by PMD AtLeastOneConstructor rule
    }
    /**
     * Provides a BCrypt-based {@link PasswordEncoder} bean for hashing and
     * verifying user passwords throughout the application.
     * Cost factor 12 is used to balance security and performance.
     *
     * @return a BCryptPasswordEncoder instance with strength 12
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
