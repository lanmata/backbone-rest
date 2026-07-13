/*
 *  @(#)PasswordPolicyViolation.java
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
package com.umdc.backoffice.v1.iam.passwords.api.to;

/**
 * Represents a single password policy rule violation.
 *
 * @param rule    the identifier of the violated rule (e.g. {@code "MIN_LENGTH"})
 * @param message a human-readable description of the violation
 */
public record PasswordPolicyViolation(String rule, String message) {
}

