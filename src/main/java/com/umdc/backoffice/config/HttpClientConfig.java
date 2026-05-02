package com.umdc.backoffice.config;

import com.umdc.backoffice.security.exception.CertificateSecurityException;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;

/// HTTP client configuration providing an SSL-aware {@link RestTemplate} for
/// all outbound inter-service calls within the {@code *.tst} domain.
///
/// <p>The trust material is loaded from {@code prx-truststore.jks}, which is
/// signed by the PRX Internal CA.  All {@code *.tst} service hostnames
/// (e.g. {@code prx-qa.config-server.tst}, {@code prx-qa.manager.tst}) are
/// therefore accepted during TLS handshake without disabling hostname
/// verification.</p>
///
/// <p>This bean is declared {@code @Primary} so that it satisfies any
/// unqualified {@link RestTemplate} injection point in the application
/// context — including the {@code JwtDecoder} in {@code SecurityConfig} —
/// without requiring changes to existing classes.</p>
///
/// @version 1.0
/// @since 1.0
@Configuration
public class HttpClientConfig {

    /// Classpath-relative path to the PRX Internal CA trust store.
    private final String trustStoreLocation;

    /// Password used to unlock the PRX trust store.
    private final String trustStorePassword;

    /// Creates a new {@code HttpClientConfig}.
    ///
    /// @param trustStoreLocation classpath-relative path to {@code prx-truststore.jks};
    ///                           defaults to {@code prx-truststore.jks}
    /// @param trustStorePassword password for the trust store;
    ///                           resolved from {@code ${SSL_TRUSTSTORE_PASSWORD}} and
    ///                           defaults to {@code changeit}
    public HttpClientConfig(
            @Value("${prx.security.truststore.location:prx-truststore.jks}") String trustStoreLocation,
            @Value("${prx.security.truststore.password:changeit}") String trustStorePassword) {
        this.trustStoreLocation = trustStoreLocation;
        this.trustStorePassword = trustStorePassword;
    }

    /// Creates a {@link RestTemplate} whose HTTPS connections verify server certificates
    /// against the PRX Internal CA trust store ({@code prx-truststore.jks}).
    ///
    /// <p>The {@link SSLContext} is built via Apache HttpClient 5's
    /// {@link SSLContextBuilder}, loading trust material from the classpath
    /// resource.  A pooled {@link CloseableHttpClient} wraps the resulting
    /// {@link SSLConnectionSocketFactory} and is registered as the request
    /// factory for the returned template.</p>
    ///
    /// <p>The bean is marked {@code @Primary} so it satisfies unqualified
    /// {@link RestTemplate} injection points (e.g. {@code JwtDecoder}) in
    /// the application context.</p>
    ///
    /// @return SSL-aware {@link RestTemplate} backed by Apache HttpClient 5
    /// @throws CertificateSecurityException when the trust store cannot be loaded or the
    ///                                      {@link SSLContext} cannot be initialised
    @Bean
    @Primary
    public RestTemplate interServiceRestTemplate() throws CertificateSecurityException {
        try {
            Resource trustStoreResource = new ClassPathResource(trustStoreLocation);
            SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial(trustStoreResource.getURL(), trustStorePassword.toCharArray())
                    .build();

            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext);
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setConnectionManager(
                            PoolingHttpClientConnectionManagerBuilder.create()
                                    .setSSLSocketFactory(sslSocketFactory)
                                    .build())
                    .build();

            return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
        } catch (Exception e) {
            throw new CertificateSecurityException(
                    "Failed to initialise inter-service RestTemplate SSL context", e);
        }
    }
}

