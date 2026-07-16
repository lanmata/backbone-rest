/*
 *  @(#)NoticeRequest.java
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

import com.umdc.commons.general.to.Request;

/**
 * Data transfer object for notice operations.
 * Extends the base Request class.
 */
public class NoticeRequest extends Request {

    private Notice notice;

    /**
     * Default constructor.
     */
    public NoticeRequest() {
        super();
    }

    /**
     * Gets the notice.
     *
     * @return the notice
     */
    public Notice getNotice() {
        return notice;
    }

    /**
     * Sets the notice.
     *
     * @param notice the notice to set
     */
    public void setNotice(Notice notice) {
        this.notice = notice;
    }
}
