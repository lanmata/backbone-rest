/*
 *  @(#)NoticeService.java
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
package com.umdc.backoffice.v1.notices.service;

import com.umdc.backoffice.v1.notices.api.to.Notice;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

/**
 * Interface for notice operations.
 * <p>
 * A notice has no surrogate id and nothing mutable besides its existence: its
 * identity is the composite of {@code userId}, {@code applicationId} and
 * {@code noticeTypeId}. There is therefore no single-item lookup and no update
 * operation, only creation, listing by application and deletion by composite key.
 * </p>
 */
public interface NoticeService {

    /**
     * Creates a new notice.
     *
     * @param notice the notice to create
     * @return the created notice wrapped in a ResponseEntity
     * @throws NotImplementedException if the method is not implemented
     */
    default ResponseEntity<Notice> create(Notice notice) {
        throw new NotImplementedException();
    }

    /**
     * Lists notices belonging to a given application.
     *
     * @param applicationId the application ID whose notices are requested
     * @return the notices wrapped in a ResponseEntity
     * @throws NotImplementedException if the method is not implemented
     */
    default ResponseEntity<List<Notice>> listByApplication(UUID applicationId) {
        throw new NotImplementedException();
    }

    /**
     * Deletes a notice identified by its composite key.
     *
     * @param userId        the user ID
     * @param applicationId the application ID
     * @param noticeTypeId  the notice type ID
     * @return a ResponseEntity reflecting the outcome of the deletion
     * @throws NotImplementedException if the method is not implemented
     */
    default ResponseEntity<?> delete(UUID userId, UUID applicationId, UUID noticeTypeId) {
        throw new NotImplementedException();
    }
}
