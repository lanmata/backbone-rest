/*
 *  @(#)PasswordPolicyConfig.java
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
package com.umdc.backoffice.v1.iam.passwords.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the password policy.
 * <p>
 * Binds to the {@code umdc.password-policy} prefix in application configuration.
 * All fields have sensible defaults that enforce a reasonably strong policy.
 * </p>
 */
@Configuration
@ConfigurationProperties(prefix = "umdc.password-policy")
public class PasswordPolicyConfig {

    private int minLength = 8;
    private int maxLength = 128;
    private boolean requireUppercase = true;
    private boolean requireLowercase = true;
    private boolean requireDigit = true;
    private boolean requireSpecialChar = true;
    private String specialChars = "!@#$%^&*()_+-=[]{}|;':\",./<>?";

    /**
     * Default constructor.
     */
    public PasswordPolicyConfig() {
        // Default constructor
    }

    /**
     * Returns the minimum required password length.
     *
     * @return minimum password length (default: 8)
     */
    public int getMinLength() {
        return minLength;
    }

    /**
     * Sets the minimum required password length.
     *
     * @param minLength the minimum length
     */
    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    /**
     * Returns the maximum allowed password length.
     *
     * @return maximum password length (default: 128)
     */
    public int getMaxLength() {
        return maxLength;
    }

    /**
     * Sets the maximum allowed password length.
     *
     * @param maxLength the maximum length
     */
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * Returns whether an uppercase letter is required.
     *
     * @return {@code true} if uppercase is required (default: true)
     */
    public boolean isRequireUppercase() {
        return requireUppercase;
    }

    /**
     * Sets whether an uppercase letter is required.
     *
     * @param requireUppercase {@code true} to require uppercase
     */
    public void setRequireUppercase(boolean requireUppercase) {
        this.requireUppercase = requireUppercase;
    }

    /**
     * Returns whether a lowercase letter is required.
     *
     * @return {@code true} if lowercase is required (default: true)
     */
    public boolean isRequireLowercase() {
        return requireLowercase;
    }

    /**
     * Sets whether a lowercase letter is required.
     *
     * @param requireLowercase {@code true} to require lowercase
     */
    public void setRequireLowercase(boolean requireLowercase) {
        this.requireLowercase = requireLowercase;
    }

    /**
     * Returns whether a digit is required.
     *
     * @return {@code true} if a digit is required (default: true)
     */
    public boolean isRequireDigit() {
        return requireDigit;
    }

    /**
     * Sets whether a digit is required.
     *
     * @param requireDigit {@code true} to require a digit
     */
    public void setRequireDigit(boolean requireDigit) {
        this.requireDigit = requireDigit;
    }

    /**
     * Returns whether a special character is required.
     *
     * @return {@code true} if a special char is required (default: true)
     */
    public boolean isRequireSpecialChar() {
        return requireSpecialChar;
    }

    /**
     * Sets whether a special character is required.
     *
     * @param requireSpecialChar {@code true} to require a special char
     */
    public void setRequireSpecialChar(boolean requireSpecialChar) {
        this.requireSpecialChar = requireSpecialChar;
    }

    /**
     * Returns the set of characters considered "special".
     *
     * @return the special characters string
     */
    public String getSpecialChars() {
        return specialChars;
    }

    /**
     * Sets the set of characters considered "special".
     *
     * @param specialChars the special characters string
     */
    public void setSpecialChars(String specialChars) {
        this.specialChars = specialChars;
    }
}

