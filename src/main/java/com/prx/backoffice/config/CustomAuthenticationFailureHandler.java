///*
// * @(#)CustomAuthenticationFailureHandler.java.
// *
// * Copyright (c) Luis Antonio Mata Mata. All rights reserved.
// *
// * All rights to this product are owned by Luis Antonio Mata Mata and may only
// * be used under the terms of its associated license document. You may NOT
// * copy, modify, sublicense, or distribute this source file or portions of
// * it unless previously authorized in writing by Luis Antonio Mata Mata.
// * In any event, this notice and the above copyright must always be included
// * verbatim with this file.
// */
//
//package com.prx.backoffice.config;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.web.authentication.AuthenticationFailureHandler;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.util.Calendar;
//
///**
// * CustomAuthenticationFailureHandler.
// *
// * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
// * @version 1.0.0, 10-03-2021
// */
//
//public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
//
//    @Override
//    public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
//        httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
//
//        String jsonPayload = "{\"message\" : \"%s\", \"timestamp\" : \"%s\" }";
//        httpServletResponse.getOutputStream().println(String.format(jsonPayload, e.getMessage(), Calendar.getInstance().getTime()));
//    }
//}
