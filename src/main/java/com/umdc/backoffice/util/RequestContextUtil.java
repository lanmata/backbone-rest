/*
 *  @(#)RequestContextUtil.java
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
package com.umdc.backoffice.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Resolves source IP address and User-Agent from the current request thread.
 * Shared by any service that records request context in an audit trail —
 * previously duplicated privately in {@code SessionServiceImpl}.
 */
@Component
public class RequestContextUtil {

    /**
     * Resolves the originating client IP, preferring {@code X-Forwarded-For} (set by the
     * load balancer / reverse proxy) over {@link HttpServletRequest#getRemoteAddr()}.
     *
     * @return the source IP address, or {@code null} when called outside a request thread
     */
    public String extractIpAddress() {
        try {
            var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return null;
            }
            HttpServletRequest request = attrs.getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            return (forwarded != null && !forwarded.isBlank())
                    ? forwarded.split(",")[0].trim()
                    : request.getRemoteAddr();
        } catch (RuntimeException _) {
            return null;
        }
    }

    /**
     * @return the {@code User-Agent} header of the current request, or {@code null} when
     *         called outside a request thread
     */
    public String extractUserAgent() {
        try {
            var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return null;
            }
            return attrs.getRequest().getHeader("User-Agent");
        } catch (RuntimeException _) {
            return null;
        }
    }
}
