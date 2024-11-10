package com.prx.backoffice.config.auth;

import com.prx.backoffice.v1.session.interceptor.SessionJwtInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * SessionJwtWebConfigurer.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 2024-11-10
 * @since 17
 */
@Configuration
public class SessionJwtWebConfigurer implements WebMvcConfigurer {

    private final SessionJwtInterceptor sessionJwtInterceptor;

    public SessionJwtWebConfigurer(SessionJwtInterceptor sessionJwtInterceptor) {
        this.sessionJwtInterceptor = sessionJwtInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionJwtInterceptor)
                .addPathPatterns("/v1/**")
                .excludePathPatterns("/v1/session/token", "/v1/session/validate");
    }


}

