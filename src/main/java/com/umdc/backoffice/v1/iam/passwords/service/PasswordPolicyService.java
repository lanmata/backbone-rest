/*
 *  @(#)PasswordPolicyService.java
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

import java.util.List;

/// Service for validating raw passwords against the configured password policy.
public interface PasswordPolicyService {

    /// Validates the given raw password against all configured policy rules.
    ///
    /// @param rawPassword the plain-text password to validate
    /// @return an empty list if the password satisfies all rules; otherwise a list of violations
    List<PasswordPolicyViolation> validate(String rawPassword);

    /// Returns {@code true} if the password passes all policy rules.
    ///
    /// @param rawPassword the plain-text password to check
    /// @return {@code true} when valid; {@code false} when one or more violations exist
    boolean isValid(String rawPassword);
}

