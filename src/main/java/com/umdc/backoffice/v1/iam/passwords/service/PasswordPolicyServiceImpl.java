/*
 *  @(#)PasswordPolicyServiceImpl.java
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
package com.umdc.backoffice.v1.iam.passwords.service;

import com.umdc.backoffice.v1.iam.passwords.api.to.PasswordPolicyViolation;
import com.umdc.backoffice.v1.iam.passwords.config.PasswordPolicyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of {@link PasswordPolicyService}.
 * <p>
 * Checks a raw password against each rule configured in {@link PasswordPolicyConfig}
 * and collects all violations found. Each failing rule contributes one
 * {@link PasswordPolicyViolation} entry with a clear, human-readable message.
 * </p>
 */
@Service
public class PasswordPolicyServiceImpl implements PasswordPolicyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordPolicyServiceImpl.class);

    // Log message template (constant avoids PMD AvoidDuplicateLiterals)
    private static final String LOG_VIOLATION_MSG = "Password policy violation [{}]: {}";

    // Rule identifiers
    private static final String RULE_MIN_LENGTH = "MIN_LENGTH";
    private static final String RULE_MAX_LENGTH = "MAX_LENGTH";
    private static final String RULE_UPPERCASE = "UPPERCASE";
    private static final String RULE_LOWERCASE = "LOWERCASE";
    private static final String RULE_DIGIT = "DIGIT";
    private static final String RULE_SPECIAL_CHAR = "SPECIAL_CHAR";

    // Message fragments (constants avoid PMD AvoidDuplicateLiterals)
    private static final String MSG_NULL_OR_EMPTY = "Password must not be null or empty";
    private static final String MSG_MIN_LENGTH_PREFIX = "Password must be at least ";
    private static final String MSG_MIN_LENGTH_SUFFIX = " characters long";
    private static final String MSG_MAX_LENGTH_PREFIX = "Password must not exceed ";
    private static final String MSG_MAX_LENGTH_SUFFIX = " characters";
    private static final String MSG_UPPERCASE = "Password must contain at least one uppercase letter";
    private static final String MSG_LOWERCASE = "Password must contain at least one lowercase letter";
    private static final String MSG_DIGIT = "Password must contain at least one digit";
    private static final String MSG_SPECIAL_CHAR = "Password must contain at least one special character";

    private final PasswordPolicyConfig config;

    /**
     * Constructs a new {@code PasswordPolicyServiceImpl}.
     *
     * @param config the password policy configuration properties
     */
    public PasswordPolicyServiceImpl(PasswordPolicyConfig config) {
        this.config = config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PasswordPolicyViolation> validate(String rawPassword) {
        List<PasswordPolicyViolation> violations = new ArrayList<>();

        if (Objects.isNull(rawPassword) || rawPassword.isEmpty()) {
            violations.add(new PasswordPolicyViolation(RULE_MIN_LENGTH, MSG_NULL_OR_EMPTY));
            LOGGER.debug("Password policy violation: {}", MSG_NULL_OR_EMPTY);
            return violations;
        }

        int length = rawPassword.length();

        if (length < config.getMinLength()) {
            String msg = MSG_MIN_LENGTH_PREFIX + config.getMinLength() + MSG_MIN_LENGTH_SUFFIX;
            violations.add(new PasswordPolicyViolation(RULE_MIN_LENGTH, msg));
            LOGGER.debug(LOG_VIOLATION_MSG, RULE_MIN_LENGTH, msg);
        }

        if (length > config.getMaxLength()) {
            String msg = MSG_MAX_LENGTH_PREFIX + config.getMaxLength() + MSG_MAX_LENGTH_SUFFIX;
            violations.add(new PasswordPolicyViolation(RULE_MAX_LENGTH, msg));
            LOGGER.debug(LOG_VIOLATION_MSG, RULE_MAX_LENGTH, msg);
        }

        if (config.isRequireUppercase() && rawPassword.chars().noneMatch(Character::isUpperCase)) {
            violations.add(new PasswordPolicyViolation(RULE_UPPERCASE, MSG_UPPERCASE));
            LOGGER.debug(LOG_VIOLATION_MSG, RULE_UPPERCASE, MSG_UPPERCASE);
        }

        if (config.isRequireLowercase() && rawPassword.chars().noneMatch(Character::isLowerCase)) {
            violations.add(new PasswordPolicyViolation(RULE_LOWERCASE, MSG_LOWERCASE));
            LOGGER.debug(LOG_VIOLATION_MSG, RULE_LOWERCASE, MSG_LOWERCASE);
        }

        if (config.isRequireDigit() && rawPassword.chars().noneMatch(Character::isDigit)) {
            violations.add(new PasswordPolicyViolation(RULE_DIGIT, MSG_DIGIT));
            LOGGER.debug(LOG_VIOLATION_MSG, RULE_DIGIT, MSG_DIGIT);
        }

        if (config.isRequireSpecialChar()) {
            String specials = config.getSpecialChars();
            boolean hasSpecial = rawPassword.chars().anyMatch(c -> specials.indexOf(c) >= 0);
            if (!hasSpecial) {
                violations.add(new PasswordPolicyViolation(RULE_SPECIAL_CHAR, MSG_SPECIAL_CHAR));
                LOGGER.debug(LOG_VIOLATION_MSG, RULE_SPECIAL_CHAR, MSG_SPECIAL_CHAR);
            }
        }

        return violations;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid(String rawPassword) {
        return validate(rawPassword).isEmpty();
    }
}

