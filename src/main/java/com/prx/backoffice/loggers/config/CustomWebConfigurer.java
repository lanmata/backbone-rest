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
package com.prx.backoffice.loggers.config;

import com.prx.backoffice.loggers.interceptor.InterceptorLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CustomWebConfigurer.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 15-05-2022
 * @since 11
 */
@Component
@RequiredArgsConstructor
public class CustomWebConfigurer implements WebMvcConfigurer {

    private final InterceptorLog interceptorLog;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptorLog);
    }
}
