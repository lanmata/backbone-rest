package com.umdc.backoffice.v1.iam.passwords.service;

import com.umdc.backoffice.v1.iam.passwords.api.to.PasswordPolicyViolation;
import com.umdc.backoffice.v1.iam.passwords.config.PasswordPolicyConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordPolicyServiceImplTest {

    // Rule identifier constants (mirror production values)
    private static final String RULE_MIN_LENGTH   = "MIN_LENGTH";
    private static final String RULE_MAX_LENGTH   = "MAX_LENGTH";
    private static final String RULE_UPPERCASE    = "UPPERCASE";
    private static final String RULE_LOWERCASE    = "LOWERCASE";
    private static final String RULE_DIGIT        = "DIGIT";
    private static final String RULE_SPECIAL_CHAR = "SPECIAL_CHAR";

    // Test-password constants (avoid duplicate literals across methods)
    private static final String VALID_PASSWORD       = "ValidP@ss1";
    private static final String SHORT_PASSWORD       = "short";
    private static final String BAD_PASSWORD         = "bad";
    private static final String NO_UPPERCASE_PWD     = "nouppercase1!";
    private static final String NO_LOWERCASE_PWD     = "NOLOWERCASE1!";
    private static final String NO_DIGIT_PWD         = "NoDigit!!";
    private static final String NO_SPECIAL_CHAR_PWD  = "NoSpecial1A";

    private PasswordPolicyServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PasswordPolicyServiceImpl(new PasswordPolicyConfig());
    }

    // ── null / blank ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("null password returns at least one violation")
    void shouldReturnViolationWhenPasswordIsNull() {
        List<PasswordPolicyViolation> violations = service.validate(null);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> RULE_MIN_LENGTH.equals(v.rule())));
    }

    @Test
    @DisplayName("blank password returns a violation")
    void shouldReturnViolationWhenPasswordIsBlank() {
        List<PasswordPolicyViolation> violations = service.validate("");

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> RULE_MIN_LENGTH.equals(v.rule())));
    }

    // ── length rules ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("password shorter than minLength triggers MIN_LENGTH violation")
    void shouldReturnViolationWhenPasswordTooShort() {
        List<PasswordPolicyViolation> violations = service.validate(SHORT_PASSWORD);

        assertTrue(violations.stream().anyMatch(v -> RULE_MIN_LENGTH.equals(v.rule())));
    }

    @Test
    @DisplayName("password longer than 128 characters triggers MAX_LENGTH violation")
    void shouldReturnViolationWhenPasswordTooLong() {
        // "averylongpassword" × 10 = 170 chars — exceeds default maxLength of 128
        String overLong = "averylongpassword".repeat(10);
        List<PasswordPolicyViolation> violations = service.validate(overLong);

        assertTrue(violations.stream().anyMatch(v -> RULE_MAX_LENGTH.equals(v.rule())));
    }

    // ── character-class rules ──────────────────────────────────────────────────

    @Test
    @DisplayName("valid password produces no violations")
    void shouldReturnNoViolationsForValidPassword() {
        List<PasswordPolicyViolation> violations = service.validate(VALID_PASSWORD);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("password with no uppercase letter triggers UPPERCASE violation")
    void shouldReturnViolationWhenMissingUppercase() {
        List<PasswordPolicyViolation> violations = service.validate(NO_UPPERCASE_PWD);

        assertTrue(violations.stream().anyMatch(v -> RULE_UPPERCASE.equals(v.rule())));
    }

    @Test
    @DisplayName("password with no lowercase letter triggers LOWERCASE violation")
    void shouldReturnViolationWhenMissingLowercase() {
        List<PasswordPolicyViolation> violations = service.validate(NO_LOWERCASE_PWD);

        assertTrue(violations.stream().anyMatch(v -> RULE_LOWERCASE.equals(v.rule())));
    }

    @Test
    @DisplayName("password with no digit triggers DIGIT violation")
    void shouldReturnViolationWhenMissingDigit() {
        List<PasswordPolicyViolation> violations = service.validate(NO_DIGIT_PWD);

        assertTrue(violations.stream().anyMatch(v -> RULE_DIGIT.equals(v.rule())));
    }

    @Test
    @DisplayName("password with no special character triggers SPECIAL_CHAR violation")
    void shouldReturnViolationWhenMissingSpecialChar() {
        List<PasswordPolicyViolation> violations = service.validate(NO_SPECIAL_CHAR_PWD);

        assertTrue(violations.stream().anyMatch(v -> RULE_SPECIAL_CHAR.equals(v.rule())));
    }

    // ── isValid ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("isValid returns true for a password that satisfies all rules")
    void isValidReturnsTrueForValidPassword() {
        assertTrue(service.isValid(VALID_PASSWORD));
    }

    @Test
    @DisplayName("isValid returns false for a password that violates rules")
    void isValidReturnsFalseForBadPassword() {
        assertFalse(service.isValid(BAD_PASSWORD));
    }

    // ── policy flag overrides ──────────────────────────────────────────────────

    @Test
    @DisplayName("requireUppercase=false suppresses the UPPERCASE violation")
    void shouldNotReturnUppercaseViolationWhenUppercaseNotRequired() {
        PasswordPolicyConfig relaxed = new PasswordPolicyConfig();
        relaxed.setRequireUppercase(false);
        PasswordPolicyServiceImpl relaxedService = new PasswordPolicyServiceImpl(relaxed);

        List<PasswordPolicyViolation> violations = relaxedService.validate(NO_UPPERCASE_PWD);

        assertTrue(violations.stream().noneMatch(v -> RULE_UPPERCASE.equals(v.rule())));
    }
}

