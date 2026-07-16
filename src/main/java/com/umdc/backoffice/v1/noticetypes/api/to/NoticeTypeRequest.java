/*
 *  @(#)NoticeTypeRequest.java
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
package com.umdc.backoffice.v1.noticetypes.api.to;

import com.umdc.commons.general.to.Request;

/**
 * Data transfer object for notice type operations.
 * Extends the base Request class.
 */
public class NoticeTypeRequest extends Request {

    private NoticeType noticeType;

    /**
     * Default constructor.
     */
    public NoticeTypeRequest() {
        super();
    }

    /**
     * Gets the notice type.
     *
     * @return the notice type
     */
    public NoticeType getNoticeType() {
        return noticeType;
    }

    /**
     * Sets the notice type.
     *
     * @param noticeType the notice type to set
     */
    public void setNoticeType(NoticeType noticeType) {
        this.noticeType = noticeType;
    }
}
