/*
 *  @(#)PermissionCheckResponse.java
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
package com.umdc.backoffice.v1.iam.permissions.api.to;

/**
 * Response payload for the IAM permission-check endpoint.
 *
 * @param granted    {@code true} if the session token carries the requested permission
 * @param permission the permission string that was evaluated
 * @param reason     human-readable explanation of the result
 */
public record PermissionCheckResponse(boolean granted, String permission, String reason) {
}

