/*
 *  @(#)Notice.java
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
package com.umdc.backoffice.v1.notices.api.to;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Data transfer object representing a notice.
 * Mirrors the {@code Notice} schema declared in {@code api.yaml}.
 * <p>
 * A notice has no surrogate id: its identity is the composite of
 * {@code userId}, {@code applicationId} and {@code noticeTypeId}.
 * </p>
 */
public class Notice implements Serializable {

    private Instant createdAt;
    private UUID userId;
    private UUID applicationId;
    private UUID noticeTypeId;

    /**
     * Default constructor.
     */
    public Notice() {
        // Default constructor
    }

    /**
     * Gets the creation timestamp.
     *
     * @return the creation timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp.
     *
     * @param createdAt the creation timestamp to set
     */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the user id.
     *
     * @return the user id
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * Sets the user id.
     *
     * @param userId the user id to set
     */
    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    /**
     * Gets the application id.
     *
     * @return the application id
     */
    public UUID getApplicationId() {
        return applicationId;
    }

    /**
     * Sets the application id.
     *
     * @param applicationId the application id to set
     */
    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * Gets the notice type id.
     *
     * @return the notice type id
     */
    public UUID getNoticeTypeId() {
        return noticeTypeId;
    }

    /**
     * Sets the notice type id.
     *
     * @param noticeTypeId the notice type id to set
     */
    public void setNoticeTypeId(UUID noticeTypeId) {
        this.noticeTypeId = noticeTypeId;
    }
}
