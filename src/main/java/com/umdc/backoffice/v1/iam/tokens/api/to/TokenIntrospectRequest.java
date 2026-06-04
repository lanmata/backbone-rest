/*
 *  @(#)TokenIntrospectRequest.java
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
package com.umdc.backoffice.v1.iam.tokens.api.to;

import jakarta.validation.constraints.NotBlank;

/// Request body for the token introspection endpoint.
///
/// @param token the JWT session token to introspect
public record TokenIntrospectRequest(
        @NotBlank String token
) {
}

