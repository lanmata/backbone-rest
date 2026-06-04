//package com.umdc.backoffice.config;
//
//import com.umdc.backoffice.security.exception.CertificateSecurityException;
//import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
//import org.apache.hc.client5.http.impl.classic.HttpClients;
//import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
//import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
//import org.apache.hc.core5.ssl.SSLContextBuilder;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.core.io.Resource;
//import org.springframework.core.io.ResourceLoader;
//import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
//import org.springframework.web.client.RestTemplate;
//
//import javax.net.ssl.SSLContext;
//
///// HTTP client configuration providing an SSL-aware {@link RestTemplate} for
///// all outbound inter-service calls within the {@code *.tst} domain.
/////
///// <p>The trust material is loaded from the trust store configured via
///// {@code umdc.security.truststore.location}, which is signed by the PRX
///// Internal CA.  All {@code *.tst} service hostnames
///// (e.g. {@code prx-qa.config-server.tst}, {@code prx-qa.manager.tst}) are
///// therefore accepted during TLS handshake without disabling hostname
///// verification.</p>
/////
///// <p>This bean is declared {@code @Primary} so that it satisfies any
///// unqualified {@link RestTemplate} injection point in the application
///// context — including the {@code JwtDecoder} in {@code SecurityConfig} —
///// without requiring changes to existing classes.</p>
/////
///// @version 1.0
///// @since 1.0
//@Configuration
//public class HttpClientConfig {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientConfig.class);
//
//    /// Classpath-relative path (or {@code classpath:}-prefixed path) to the PRX
//    /// Internal CA trust store.
//    private final String trustStoreLocation;
//
//    /// Password used to unlock the PRX trust store.
//    private final String trustStorePassword;
//
//    /// Spring {@link ResourceLoader} used to resolve {@code classpath:},
//    /// {@code file:}, and bare-path resource URIs uniformly.
//    private final ResourceLoader resourceLoader;
//
//    /// Creates a new {@code HttpClientConfig}.
//    ///
//    /// @param trustStoreLocation resource location of the trust store; supports
//    ///                           {@code classpath:} and {@code file:} prefixes as
//    ///                           well as bare classpath-relative names.
//    ///                           Resolved from {@code umdc.security.truststore.location};
//    ///                           defaults to {@code classpath:umdc-truststore.jks}
//    /// @param trustStorePassword password for the trust store;
//    ///                           resolved from {@code umdc.security.truststore.password}
//    ///                           and defaults to {@code changeit}
//    /// @param resourceLoader     Spring resource loader injected by the container
//    public HttpClientConfig(
//            @Value("${umdc.security.truststore.location:classpath:umdc-truststore.jks}") String trustStoreLocation,
//            @Value("${umdc.security.truststore.password:changeit}") String trustStorePassword,
//            ResourceLoader resourceLoader) {
//        this.trustStoreLocation = trustStoreLocation;
//        this.trustStorePassword = trustStorePassword;
//        this.resourceLoader = resourceLoader;
//        LOGGER.info("HttpClientConfig initialised — truststore location: {}", trustStoreLocation);
//    }
//
//    /// Creates a {@link RestTemplate} whose HTTPS connections verify server certificates
//    /// against the PRX Internal CA trust store.
//    ///
//    /// <p>The {@link SSLContext} is built via Apache HttpClient 5's
//    /// {@link SSLContextBuilder}, loading trust material from the resource resolved
//    /// by the injected {@link ResourceLoader} — which correctly handles
//    /// {@code classpath:} and {@code file:} URI prefixes.  A pooled
//    /// {@link CloseableHttpClient} wraps the resulting
//    /// {@link SSLConnectionSocketFactory} and is registered as the request
//    /// factory for the returned template.</p>
//    ///
//    /// <p>The bean is marked {@code @Primary} so it satisfies unqualified
//    /// {@link RestTemplate} injection points (e.g. {@code JwtDecoder}) in
//    /// the application context.</p>
//    ///
//    /// @return SSL-aware {@link RestTemplate} backed by Apache HttpClient 5
//    /// @throws CertificateSecurityException when the trust store cannot be loaded or the
//    ///                                      {@link SSLContext} cannot be initialised
//    @Bean
//    @Primary
//    public RestTemplate interServiceRestTemplate() throws CertificateSecurityException {
//        LOGGER.info("Building inter-service RestTemplate with SSL context");
//        try {
//            LOGGER.debug("Resolving truststore resource from location: {}", trustStoreLocation);
//            Resource trustStoreResource = resourceLoader.getResource(trustStoreLocation);
//            LOGGER.debug("Truststore resource resolved — exists: {}, URI: {}",
//                    trustStoreResource.exists(),
//                    trustStoreResource.exists() ? trustStoreResource.getURI() : "N/A");
//
//            LOGGER.debug("Loading trust material and building SSLContext");
//            SSLContext sslContext = SSLContextBuilder.create()
//                    .loadTrustMaterial(trustStoreResource.getURL(), trustStorePassword.toCharArray())
//                    .build();
//            LOGGER.debug("SSLContext built successfully — protocol: {}", sslContext.getProtocol());
//
//            LOGGER.debug("Creating SSLConnectionSocketFactory and pooled HttpClient");
//            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext);
//            CloseableHttpClient httpClient = HttpClients.custom()
//                    .setConnectionManager(
//                            PoolingHttpClientConnectionManagerBuilder.create()
//                                    .setSSLSocketFactory(sslSocketFactory)
//                                    .build())
//                    .build();
//
//            RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
//            LOGGER.info("Inter-service RestTemplate created successfully with SSL context");
//            return restTemplate;
//        } catch (Exception e) {
//            LOGGER.error("Failed to initialise inter-service RestTemplate SSL context — truststore: {}, error: {}",
//                    trustStoreLocation, e.getMessage(), e);
//            throw new CertificateSecurityException(
//                    "Failed to initialise inter-service RestTemplate SSL context", e);
//        }
//    }
//}
