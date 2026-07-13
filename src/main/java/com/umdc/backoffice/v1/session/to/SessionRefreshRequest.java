/*
 *  @(#)SessionRefreshRequest.java
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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request body for the {@code POST /api/v1/session/refresh} endpoint.
 *
 * @param refreshToken the refresh token issued at login time
 */
public record SessionRefreshRequest(@JsonProperty("refreshToken") String refreshToken) {
}

