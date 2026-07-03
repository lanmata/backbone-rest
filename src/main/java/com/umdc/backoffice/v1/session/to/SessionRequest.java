/*
 *  @(#)SessionRequest.java
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
package com.umdc.backoffice.v1.session.to;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request payload for alias-based session creation.
 * Carries user alias, password, and the target application context.
 *
 * @version 2.0.0, 03-07-2026
 */
public record SessionRequest(
        @NotNull @NotEmpty String alias,
        @NotNull @NotEmpty String password,
        @NotNull UUID applicationId
) {}
