/*
 *  @(#)TokenIntrospectResponse.java
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

import java.util.List;

/// Response payload for the IAM token-introspection endpoint.
///
/// @param active     {@code true} if the token is valid and not expired
/// @param subject    the JWT {@code sub} claim (user identifier)
/// @param issuer     the JWT {@code iss} claim
/// @param audience   the JWT {@code aud} claim
/// @param expiresAt  token expiry as epoch-milliseconds ({@code exp} * 1000)
/// @param issuedAt   token issuance as epoch-milliseconds ({@code iat} * 1000)
/// @param tokenType  the value of the {@code type} claim
/// @param roles      parsed list of role strings from the {@code roles} claim
public record TokenIntrospectResponse(
        boolean active,
        String subject,
        String issuer,
        String audience,
        long expiresAt,
        long issuedAt,
        String tokenType,
        List<String> roles) {
}

