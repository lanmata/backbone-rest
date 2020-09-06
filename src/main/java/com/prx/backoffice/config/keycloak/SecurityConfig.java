package com.prx.backoffice.config.keycloak;

import lombok.extern.log4j.Log4j2;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.client.KeycloakClientRequestFactory;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 * @since 2020-03-01
 */
@Log4j2
@KeycloakConfiguration
public class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {
    @Autowired
    private KeycloakSpringBootProperties properties;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
        keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
        auth.authenticationProvider(keycloakAuthenticationProvider);
    }

    @Bean
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new NullAuthenticatedSessionStrategy();
    }

    @Primary
    @Bean
    public KeycloakConfigResolver keycloakConfigResolver(KeycloakSpringBootProperties properties){
        return new CustomKeycloakSpringBootConfigResolver(properties);
    }

    @Autowired
    public KeycloakClientRequestFactory keycloakClientRequestFactory;

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public KeycloakRestTemplate keycloakRestTemplate(){
        return new KeycloakRestTemplate(keycloakClientRequestFactory);
    }

//    @Override
//    protected void configure(HttpSecurity http) throws Exception{
//        super.configure(http);
////        http.cors().and().anonymous().disable().authorizeRequests()
//        http.cors().and().authorizeRequests()
//                .antMatchers("/user/*").hasRole("rl-services-rest")
//                .anyRequest().authenticated();
//
//    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        List<String> roles = new ArrayList<>();
        List<String> methods = new ArrayList<>();
        List<String> patterns = new ArrayList<>();
        properties.getSecurityConstraints().forEach(securityConstraint ->  {
            roles.addAll(securityConstraint.getAuthRoles());
            securityConstraint.getSecurityCollections().forEach(securityCollection -> securityCollection.getMethods().forEach(s -> methods.add(s.intern())));
            securityConstraint.getSecurityCollections().forEach(securityCollection -> securityCollection.getPatterns().forEach(s -> patterns.add(s.intern())));
            log.debug("Valida accesos en configure, roles {}, metodos {}, patrones {}", roles, methods, patterns);
        });
        if(!patterns.isEmpty() && !roles.isEmpty()){
            try {
                http.anonymous().and().cors().and().authorizeRequests().
                        antMatchers("/general/*").permitAll().and().authorizeRequests()
                        .antMatchers(HttpMethod.GET, patterns.toArray(new String[patterns.size()])).hasAnyRole(roles.toArray(new String[roles.size()]))
                        .antMatchers(HttpMethod.POST, patterns.toArray(new String[patterns.size()])).hasAnyRole(roles.toArray(new String[roles.size()]))
                        .anyRequest().permitAll().and().csrf().disable();
//                http.authorizeRequests()
//                        .antMatchers(HttpMethod.GET, patterns.toArray(new String[patterns.size()])).hasAnyRole(roles.toArray(new String[roles.size()]))
//                        .antMatchers(HttpMethod.POST, patterns.toArray(new String[patterns.size()])).hasAnyRole(roles.toArray(new String[roles.size()]))
//                        .anyRequest().permitAll().and().csrf().disable();
                log.debug("PERMISOS CARGADOS, roles {}, metodos {}, patrones {}", roles, methods, patterns);
            } catch (Exception e) {
                log.warn("Ha ocurrido un error durante la carga de permisos y roles",e);
            }
        }

        log.debug("Termina llamado a configure");
    }

    @Bean
    public WebMvcConfigurer corsConfiguration() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/general/*")
                        .allowedMethods(HttpMethod.GET.toString(), HttpMethod.POST.toString(),
                                HttpMethod.PUT.toString(), HttpMethod.DELETE.toString(), HttpMethod.OPTIONS.toString())
                        .allowedOrigins("*");
            }
        };
    }

}