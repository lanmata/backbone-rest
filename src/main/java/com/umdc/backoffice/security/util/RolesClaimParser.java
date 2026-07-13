/*
 *  @(#)RolesClaimParser.java
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
package com.umdc.backoffice.security.util;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Utility for parsing the {@code roles} claim from a session JWT payload.
 * The claim is stored as the string representation of a Java collection,
 * e.g. {@code [ROLE_ADMIN, ROLE_USER]}.  This class provides a single
 * canonical parser so the logic is not duplicated across filters and services.
 *
 * @version 1.0
 * @since 1.0
 */
public final class RolesClaimParser {

    private RolesClaimParser() {
        // utility class — no instances
    }

    /**
     * Parses the {@code roles} claim object into a list of trimmed role strings.
     *
     * @param rolesObj the raw claim value (may be {@code null}, a {@link java.util.List},
     *                 or a string representation such as {@code "[ROLE_A, ROLE_B]"})
     * @return a non-null, possibly-empty list of role strings
     */
    public static List<String> parseRoles(Object rolesObj) {
        if (rolesObj == null) {
            return List.of();
        }
        if (rolesObj instanceof List<?> list) {
            return list.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toList();
        }
        String rolesStr = rolesObj.toString()
                .replace("[", "")
                .replace("]", "");
        if (rolesStr.isBlank()) {
            return List.of();
        }
        return Arrays.stream(rolesStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}

