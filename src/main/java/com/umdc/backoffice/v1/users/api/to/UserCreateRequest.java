/*
 *  @(#)UserCreateRequest.java
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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.umdc.commons.general.pojo.Person;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * UserCreateRequest is a record that represents the request payload for creating a new user.
 * It contains user details such as alias, password, active status, personal information, roles, and service ID.
 *
 * @param id            The unique identifier for the user.
 * @param alias         The unique alias for the user.
 * @param password      The password for the user.
 * @param active        Indicates if the user is active.
 * @param person        The person identifier for the user.
 * @param roleId        The roles assigned to the user.
 * @param applicationId The application identifier for the user.
 */
@JsonPropertyOrder({
        "id",
        "alias",
        "displayName",
        "password",
        "email",
        "notificationEmail",
        "notificationSms",
        "privacyDataOutActive",
        "person",
        "roleId",
        "applicationId",
        "active"
})
public record UserCreateRequest(
        /**
         * The unique identifier for the user.
         */
        UUID id,

        /**
         * The unique alias for the user.
         */
        @NotNull @NotBlank
        String alias,

        /**
         *  Name to display
         */
        @NotNull @NotBlank
        String displayName,

        /**
         * The password for the user.
         */
        @NotNull @NotBlank
        String password,

        /**
         * The email for the user.
         */
        @NotNull @NotBlank @Email
        String email,

        /**
         * Indicates if the notification email is enabled
         */
        boolean notificationEmail,

        /**
         * Indicates if the notification sms is enabled
         */
        boolean notificationSms,

        /**
         * Indicates if the share data is enabled
         */
        boolean privacyDataOutActive,

        /**
         * Indicates if the user is active.
         */
        boolean active,

        /**
         * The person identifier for the user.
         */
        @NotNull
        Person person,

        /**
         * The roles assigned to the user.
         */
        @NotNull
        UUID roleId,

        /**
         * The application identifier for the user.
         */
        @NotNull
        UUID applicationId) {
}
