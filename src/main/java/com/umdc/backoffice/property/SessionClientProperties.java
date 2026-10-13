/*
 *  @(#)SessionClientProperties.java
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
package com.umdc.backoffice.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Which applications must present a managed client (M2M bearer) when they open, renew or refresh a user session.
 * A client that IS presented is always checked against the application, whatever these settings say.
 */
@Component
@ConfigurationProperties(prefix = "umdc.security.session.client")
public class SessionClientProperties {

    /** {@code true}: every application needs a managed client to open sessions. */
    private boolean required;

    /** Applications that need a managed client even when {@link #required} is {@code false}. */
    private List<UUID> requiredApplications = new ArrayList<>();

    public SessionClientProperties() {
        // Default constructor: bound by Spring
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public List<UUID> getRequiredApplications() {
        return requiredApplications;
    }

    public void setRequiredApplications(List<UUID> requiredApplications) {
        this.requiredApplications = Objects.isNull(requiredApplications) ? new ArrayList<>() : requiredApplications;
    }

    /**
     * @param applicationId the application of the session; {@code null} for a legacy token without the claim
     * @return whether a session of that application may only be handled for an authenticated managed client
     */
    public boolean isRequiredFor(UUID applicationId) {
        return required || (Objects.nonNull(applicationId) && requiredApplications.contains(applicationId));
    }
}
