///*
// * @(#)SecSecurityConfig.java.
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
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.access.AccessDeniedHandler;
//import org.springframework.security.web.authentication.AuthenticationFailureHandler;
//import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
//
///**
// * SecSecurityConfig.
// *
// * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
// * @version 1.0.0, 10-03-2021
// */
//
//@Configuration
//@EnableWebSecurity
//public class SecSecurityConfig extends WebSecurityConfigurerAdapter {
//
//    @Override
//    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
//        auth.inMemoryAuthentication().
//                withUser("user1").password(passwordEncoder().encode("user1Pass")).roles("USER")
//                .and()
//                .withUser("user2").password(passwordEncoder().encode("user2Pass")).roles("USER")
//                .and()
//                .withUser("admin").password(passwordEncoder().encode("adminPass")).roles("ADMIN");
//    }
//
////    @Bean
////    public LogoutSuccessHandler logoutSuccessHandler() {
////        return new CustomLogoutSuccessHandler();
////    }
////
////    @Bean
////    public AccessDeniedHandler accessDeniedHandler() {
////        return new CustomAccessDeniedHandler();
////    }
////
////    @Bean
////    public AuthenticationFailureHandler authenticationFailureHandler() {
////        return new CustomAuthenticationFailureHandler();
////    }
////
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
////
////    @Override
////    protected void configure(final HttpSecurity http) throws Exception {
////        http.csrf().disable()
////                .authorizeRequests()
////                .antMatchers("/admin/**").hasRole("ADMIN")
////                .antMatchers("/anonymous*").anonymous()
////                .antMatchers("/*").permitAll()
////                .anyRequest().authenticated() .and()
////                .formLogin()
////                .loginPage("/login.html")
////                .loginProcessingUrl("/perform_login")
////                .defaultSuccessUrl("/homepage.html", true)
////                .failureUrl("/login.html?error=true")
////                .failureHandler(authenticationFailureHandler())
////                .and()
////                .logout()
////                .logoutUrl("/perform_logout")
////                .deleteCookies("JSESSIONID")
////                .logoutSuccessHandler(logoutSuccessHandler());;
////    }
//}
