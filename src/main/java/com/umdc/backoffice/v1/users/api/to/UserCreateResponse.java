/*
 *  @(#)UserCreateResponse.java
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
package com.umdc.backoffice.v1.users.api.to;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.UUID;

/// Record representing the response for creating a user.
/// Contains user details such as ID, alias, creation date, last update, active status, person ID, role ID, and application ID.
@JsonInclude
public record UserCreateResponse(
        /// The unique identifier of the user.
        UUID id,

        /// The alias of the user.
        String alias,

        /// The email of the user.
        String email,

        /// The display name of the user.
        String displayName,

        /// The date and time when the user was created.
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdDate,

        /// The date and time when the user was last updated.
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime lastUpdate,

        /// Indicates if the user has opted to receive email notifications.
        Boolean notificationEmail,

        /// Indicates if the user has opted to receive SMS notifications.
        Boolean notificationSms,

        /// Indicates if the user has activated privacy data output.
        Boolean privacyDataOutActive,

        /// The active status of the user.
        boolean active,

        /// The unique identifier of the associated person.
        UUID personId,

        /// The unique identifier of the associated role.
        UUID roleId,

        /// The unique identifier of the associated application.
        UUID applicationId) {
}
