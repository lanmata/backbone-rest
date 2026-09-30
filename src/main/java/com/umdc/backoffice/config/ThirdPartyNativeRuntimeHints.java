package com.umdc.backoffice.config;

import com.umdc.backoffice.property.ManagementAuthenticatorProperties;
import com.umdc.backoffice.property.SecurityProperties;
import com.umdc.backoffice.property.StoreProperties;
import com.umdc.backoffice.util.KeystoreUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.cloud.vault.config.VaultProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

/**
 * Native-image reflection/proxy gaps found empirically in third-party code this project
 * depends on but doesn't control. Each entry names exactly the failure GraalVM reported.
 * Same pattern as mercury's own ThirdPartyNativeRuntimeHints (MER-5) — see that repo's
 * docs/architecture/graalvm-native-image.md.
 */
@Configuration(proxyBeanMethods = false)
@ImportRuntimeHints(ThirdPartyNativeRuntimeHints.Hints.class)
public class ThirdPartyNativeRuntimeHints {

    private ThirdPartyNativeRuntimeHints() {
     // Private constructor
    }

    private static final MemberCategory[] FULL_ACCESS = {
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS,
            MemberCategory.ACCESS_DECLARED_FIELDS
    };

    static class Hints implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // spring-cloud-vault-config ships no GraalVM reachability metadata of its own, so
            // VaultProperties' reflective no-arg-constructor binding failed at native runtime
            // with "NoSuchMethodException: VaultProperties.<init>()" despite the constructor
            // existing — identical to the gap mercury found for the same dependency.
            hints.reflection().registerType(VaultProperties.class, FULL_ACCESS);
            // Registering VaultProperties itself only covers members declared directly on that
            // class — NOT its nested static classes. VaultProperties.Ssl (trust-store /
            // trust-store-password / trust-store-type) was never registered, so the Binder's
            // reflective setter calls on it silently no-op under native-image's closed-world
            // reflection instead of throwing: no exception, just an empty Ssl with a null
            // trustStore. hasSslConfiguration() then returns false, Apache HttpClient5 skips
            // the custom TLS strategy entirely, and the Vault RestClient falls back to the
            // JDK's default cacerts — confirmed via -Djavax.net.debug=ssl:trustmanager against
            // the real Vault server: the trust manager for the Config Server call loads exactly
            // one certificate (PRX Internal CA, from spring.cloud.config.tls.trust-store — a
            // normal @ConfigurationProperties bean, covered by Spring's own AOT hint
            // generation), while the trust manager for the Vault call loads the full public CA
            // bundle (Comodo, AAA Certificate Services, ...) instead of umdc-truststore.jks's
            // single "umdc-local-ca" entry — the exact signature of "unable to find valid
            // certification path to requested target" against vault.umdc-qa.tst.
            hints.reflection().registerType(VaultProperties.Ssl.class, FULL_ACCESS);
            // backbone.jks / umdc-truststore.jks (src/main/resources) back the
            // spring.ssl.bundle.jks.backbone-rest-security bundle Tomcat's HTTPS listener uses.
            // Native-image doesn't embed arbitrary classpath resources by default — without this,
            // the classpath:backbone.jks / classpath:umdc-truststore.jks lookups Spring's own
            // JksSslStoreBundle does at runtime fail with a plain FileNotFoundException (the
            // files are real, on the classpath at build time — just never copied into the image).
            hints.resources().registerPattern("*.jks");
            // ManagedClientTokenServiceImpl.init() NPE'd loading the MCAM RS256 keypair — the
            // exact same failure shape mercury found for security-oauth's SecurityConfig tree:
            // a @ConfigurationProperties object several levels deep in a nested properties tree
            // (umdc.security.managementAuthenticator.keystore.*) came back null under
            // native-image's closed-world reflection, not a loud
            // MissingReflectionRegistrationError. Registering the full properties tree +
            // KeystoreUtil explicitly fixed it there; same fix applied here.
            hints.reflection().registerType(SecurityProperties.class, FULL_ACCESS);
            hints.reflection().registerType(ManagementAuthenticatorProperties.class, FULL_ACCESS);
            hints.reflection().registerType(StoreProperties.class, FULL_ACCESS);
            hints.reflection().registerType(KeystoreUtil.class, FULL_ACCESS);
            // com.umdc.commons-services' "requestBodyInterceptor" bean autowires a request-scoped
            // HttpServletRequest into a singleton, which Spring resolves via a JDK dynamic proxy —
            // identical gap mercury found for the same shared dependency.
            hints.proxies().registerJdkProxy(HttpServletRequest.class);
        }
    }
}
