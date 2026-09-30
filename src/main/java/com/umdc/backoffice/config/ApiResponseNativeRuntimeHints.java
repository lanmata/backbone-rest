package com.umdc.backoffice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.io.IOException;

/**
 * Every {@code *Api.java} interface in this project follows the mandatory "services return
 * {@code ResponseEntity<?>}" convention (see CLAUDE.md), which type-erases the JSON body to
 * {@code Object} at every controller method signature. Spring's AOT scanner
 * ({@code ControllerMappingReflectiveProcessor}) can only register reflection hints for a
 * response type when it can resolve a concrete class from a method signature — a wildcard
 * generic gives it nothing to resolve. Request DTOs are unaffected because they're declared as
 * concrete {@code @RequestBody} parameter types and get picked up automatically; response DTOs
 * are only ever instantiated inside a {@code *ServiceImpl} and handed back wrapped in the
 * wildcard, so they're invisible to that scanner.
 * <p>
 * Confirmed empirically against {@code ManagedClientTokenResponse}: absent from the generated
 * {@code reachability-metadata.json} while the sibling {@code ManagedClientTokenRequest} (a
 * concrete {@code @RequestBody} parameter) was present. Under the JVM this is harmless — full
 * reflection is always available — but under native-image's closed-world reflection, Jackson
 * silently serializes the unregistered type as an empty {@code {}} instead of throwing, so the
 * gap surfaces as an empty response body in the container while the same code path returns full
 * content locally.
 * <p>
 * Rather than hand-list every response DTO the way {@link ThirdPartyNativeRuntimeHints} lists
 * one-off third-party gaps, this scans every {@code api/to} (or {@code to}, for the couple of
 * domains that skip the {@code api} segment — see {@code v1/profileimage/to} and
 * {@code v1/session/to}) package under {@code v1} and registers full reflective access for every
 * DTO found, request and response alike — cheaper than maintaining two lists that would silently
 * drift apart as new endpoints are added.
 */
@Configuration(proxyBeanMethods = false)
@ImportRuntimeHints(ApiResponseNativeRuntimeHints.Hints.class)
public class ApiResponseNativeRuntimeHints {

    private static final String DTO_CLASSPATH_PATTERN = "classpath*:com/umdc/backoffice/v1/**/to/*.class";

    private ApiResponseNativeRuntimeHints() {

    }
    private static final MemberCategory[] FULL_ACCESS = {
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS,
            MemberCategory.ACCESS_DECLARED_FIELDS
    };

    static class Hints implements RuntimeHintsRegistrar {

        private static final Logger LOGGER = LoggerFactory.getLogger(Hints.class);

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(classLoader);
            MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resolver);
            int registered = 0;
            try {
                Resource[] resources = resolver.getResources(DTO_CLASSPATH_PATTERN);
                for (Resource resource : resources) {
                    MetadataReader reader = readerFactory.getMetadataReader(resource);
                    String className = reader.getClassMetadata().getClassName();
                    Class<?> dtoType = ClassUtils.resolveClassName(className, classLoader);
                    hints.reflection().registerType(dtoType, FULL_ACCESS);
                    registered++;
                }
            } catch (IOException e) {
                throw new IllegalStateException(
                        "Failed to scan '" + DTO_CLASSPATH_PATTERN + "' for native-image reflection hints", e);
            }
            LOGGER.info("Registered native-image reflection hints for {} api/to DTOs", registered);
        }
    }
}
