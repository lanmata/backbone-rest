/*
 *  @(#)NoticeGraphLookupService.java
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

import com.umdc.persistence.general.domains.NoticeEntity;

import java.util.List;
import java.util.UUID;

/**
 * Notice lookups with {@code noticeType} eagerly loaded. See
 * {@link NoticeGraphLookupServiceImpl} for why this exists instead of the plain
 * {@code NoticeRepository} query methods.
 */
public interface NoticeGraphLookupService {

    /**
     * @param applicationId the application ID
     * @return the notices belonging to that application, with {@code noticeType} eagerly loaded
     */
    List<NoticeEntity> findByApplicationIdWithGraph(UUID applicationId);
}
