/*
 *  @(#)OpenApiGenerationTest.java
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
package com.umdc.backoffice.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.mock.web.MockServletContext;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Generates the OpenAPI document from the real {@code *Api} interfaces (controllers are mocks, so
 * no service, database or Vault is needed) and checks what a client collection is built from.
 */
class OpenApiGenerationTest {

    private static JsonNode spec;

    @Configuration
    @EnableWebMvc
    @Import({SpringDocConfiguration.class, SpringDocWebMvcConfiguration.class, SpringDocConfigProperties.class,
            OpenApiConfig.class})
    static class Cfg {
        @Bean
        static BeanDefinitionRegistryPostProcessor controllerMocks() {
            return new BeanDefinitionRegistryPostProcessor() {
                @Override
                public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
                    ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
                    scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
                    scanner.findCandidateComponents("com.umdc.backoffice.v1").forEach(bd -> {
                        try {
                            Class<?> type = Class.forName(bd.getBeanClassName());
                            RootBeanDefinition def = new RootBeanDefinition();
                            def.setBeanClass(type);
                            def.setInstanceSupplier(() -> Mockito.mock(type));
                            registry.registerBeanDefinition(type.getName(), def);
                        } catch (ClassNotFoundException e) {
                            throw new IllegalStateException(e);
                        }
                    });
                }

                @Override
                public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
                    // nothing to post-process
                }
            };
        }
    }

    @BeforeAll
    static void generate() throws Exception {
        try (AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext()) {
            ctx.getEnvironment().getSystemProperties().put("springdoc.api-docs.version", "openapi_3_1");
            ctx.setServletContext(new MockServletContext());
            ctx.register(Cfg.class);
            ctx.refresh();
            org.springdoc.webmvc.api.OpenApiWebMvcResource resource = ctx.getBean(org.springdoc.webmvc.api.OpenApiWebMvcResource.class);
            byte[] json = resource.openapiJson(new org.springframework.mock.web.MockHttpServletRequest(), "/v3/api-docs",
                    java.util.Locale.ENGLISH);
            spec = new ObjectMapper().readTree(json);
        }
    }

    @Test
    void everyOperationIsGenerated() {
        Set<String> ops = new TreeSet<>();
        for (Iterator<String> p = spec.get("paths").fieldNames(); p.hasNext(); ) {
            String path = p.next();
            spec.get("paths").get(path).fieldNames().forEachRemaining(m -> ops.add(m.toUpperCase() + " " + path));
        }
        System.out.println("OPERATIONS=" + ops.size());
        ops.forEach(o -> System.out.println("OP " + o));
        assertTrue(ops.size() >= 90, "operations generated: " + ops.size());
    }

    @Test
    void publicOperationsDeclareNoSecurityAndOthersInheritBearer() {
        JsonNode login = spec.at("/paths/~1api~1v1~1session/post");
        assertTrue(login.has("security") && login.get("security").isEmpty(), "login must be public");
        assertFalse(spec.at("/paths/~1api~1v1~1users/get").has("security") && spec.at("/paths/~1api~1v1~1users/get").get("security").isEmpty());
        assertTrue(spec.at("/security/0").has("bearerAuth"));
        assertEquals("http", spec.at("/components/securitySchemes/bearerAuth/type").asText());
    }

    @Test
    void managedClientResponsesCarryTheirSchemas() {
        String ref = spec.at("/paths/~1api~1v1~1managed-clients/post/responses/201/content/application~1json/schema/$ref").asText();
        assertTrue(ref.endsWith("ManagedClientCreateResponse"), ref);
    }
}
