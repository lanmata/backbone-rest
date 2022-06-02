/*
 * @(#)$file.className.java.
 *
 * Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *
 * All rights to this product are owned by Luis Antonio Mata Mata and may only
 * be used under the terms of its associated license document. You may NOT
 * copy, modify, sublicense, or distribute this source file or portions of
 * it unless previously authorized in writing by Luis Antonio Mata Mata.
 * In any event, this notice and the above copyright must always be included
 * verbatim with this file.
 */

package com.prx.backoffice.loggers.interceptor;

import com.prx.backoffice.loggers.services.LoggingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * InterceptorLog.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 15-05-2022
 * @since 11
 */
@Component
@RequiredArgsConstructor
public class InterceptorLog implements HandlerInterceptor {

    private final LoggingService loggingService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getMethod().equals(HttpMethod.GET.name())
                ||request.getMethod().equals(HttpMethod.POST.name())
                ||request.getMethod().equals(HttpMethod.DELETE.name())
                ||request.getMethod().equals(HttpMethod.PUT.name())) {
            loggingService.displayRequest(request, null);
        }
        return true;
    }
}
