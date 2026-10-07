/*
 *  @(#)RequestContextUtilTest.java
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.mockito.Mockito.mock;

class RequestContextUtilTest {

    private final RequestContextUtil requestContextUtil = new RequestContextUtil();

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("extractIpAddress returns null outside a request thread")
    void extractIpAddress_returnsNull_whenNoRequestContext() {
        Assertions.assertNull(requestContextUtil.extractIpAddress());
    }

    @Test
    @DisplayName("extractUserAgent returns null outside a request thread")
    void extractUserAgent_returnsNull_whenNoRequestContext() {
        Assertions.assertNull(requestContextUtil.extractUserAgent());
    }

    @Test
    @DisplayName("extractIpAddress prefers the first X-Forwarded-For entry over the socket address")
    void extractIpAddress_prefersForwardedFor() {
        var request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.5, 10.0.0.1");
        request.setRemoteAddr("127.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        Assertions.assertEquals("203.0.113.5", requestContextUtil.extractIpAddress());
    }

    @Test
    @DisplayName("extractIpAddress falls back to the socket address when X-Forwarded-For is absent")
    void extractIpAddress_fallsBackToRemoteAddr() {
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        Assertions.assertEquals("127.0.0.1", requestContextUtil.extractIpAddress());
    }

    @Test
    @DisplayName("extractIpAddress falls back to the socket address when X-Forwarded-For is blank")
    void extractIpAddress_fallsBackToRemoteAddr_whenForwardedForBlank() {
        var request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "   ");
        request.setRemoteAddr("127.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        Assertions.assertEquals("127.0.0.1", requestContextUtil.extractIpAddress());
    }

    @Test
    @DisplayName("extractUserAgent returns the User-Agent header value")
    void extractUserAgent_returnsHeaderValue() {
        var request = new MockHttpServletRequest();
        request.addHeader("User-Agent", "JUnit-Agent/1.0");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        Assertions.assertEquals("JUnit-Agent/1.0", requestContextUtil.extractUserAgent());
    }

    @Test
    @DisplayName("extractIpAddress returns null when the current RequestAttributes isn't servlet-based")
    void extractIpAddress_returnsNull_onClassCastException() {
        RequestContextHolder.setRequestAttributes(mock(RequestAttributes.class));

        Assertions.assertNull(requestContextUtil.extractIpAddress());
    }

    @Test
    @DisplayName("extractUserAgent returns null when the current RequestAttributes isn't servlet-based")
    void extractUserAgent_returnsNull_onClassCastException() {
        RequestContextHolder.setRequestAttributes(mock(RequestAttributes.class));

        Assertions.assertNull(requestContextUtil.extractUserAgent());
    }
}
