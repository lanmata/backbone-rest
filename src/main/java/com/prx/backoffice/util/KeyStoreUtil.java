package com.prx.backoffice.util;

import com.prx.backoffice.config.security.SecurityProperties;
import com.prx.backoffice.config.security.StoreProperties;
import com.prx.commons.exception.CertificateSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundleKey;
import org.springframework.boot.ssl.SslStoreBundle;
import org.springframework.stereotype.Component;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Enumeration;

@Component
public class KeyStoreUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyStoreUtil.class);

    public KeyStoreUtil() {
        // Default constructor
    }

    public KeyStore getKeyStore(StoreProperties securityProperties) throws CertificateSecurityException {
        KeyStore keyStore;
        // Load Truststore
        try (InputStream keyStoreStream = getClass().getClassLoader().getResourceAsStream(securityProperties.getLocation())) {
            keyStore = KeyStore.getInstance(securityProperties.getType());
            keyStore.load(keyStoreStream, securityProperties.getPassword().toCharArray());
        } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException e) {
            LOGGER.warn("Error occurred while loading the trust store file.");
            throw new CertificateSecurityException(e);
        }
        return keyStore;
    }

    public void certificatePrint(KeyStore keystore, String filename, boolean isTrustStore) throws CertificateSecurityException {
        // Log the certificates in the truststore
        Enumeration<String> aliases;
        String from = isTrustStore ? "Truststore" : "Keystore";
        try {
            aliases = keystore.aliases();
            LOGGER.info("Loading values from {} as {}", filename, from);
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                Certificate cert = keystore.getCertificate(alias);
                LOGGER.info("""
                        --
                        Alias: {}
                        "Certificate: {}
                        """, alias, cert);
            }
        } catch (KeyStoreException e) {
            throw new CertificateSecurityException(e);
        }
    }

    public SSLContext getSSLContext(SecurityProperties securityProperties) throws NoSuchAlgorithmException, CertificateSecurityException, UnrecoverableKeyException, KeyStoreException, KeyManagementException {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

        keyManagerFactory.init(getKeyStore(securityProperties.getKeystore()), securityProperties.getKeystore().getPassword().toCharArray());
        trustManagerFactory.init(getKeyStore(securityProperties.getTruststore()));

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
        return sslContext;
    }

    public SslBundle getSslBundle(SecurityProperties securityProperties) throws CertificateSecurityException {
        return loadSslBundle(getKeyStore(securityProperties.getKeystore()),
                getKeyStore(securityProperties.getTruststore()),
                securityProperties.getKeystore().getPassword());
    }

    public SslBundle getManagementAuthenticatorSslBundle(SecurityProperties securityProperties) throws CertificateSecurityException {
        return loadSslBundle(getKeyStore(securityProperties.getManagementAuthenticator().getKeystore()),
                getKeyStore(securityProperties.getManagementAuthenticator().getTruststore()),
                securityProperties.getManagementAuthenticator().getKeystore().getPassword(),
                securityProperties.getManagementAuthenticator().getKeyAlias());
    }

    private SslBundle loadSslBundle(KeyStore trustStore, KeyStore keyStore, String keystorePassword) {
        return SslBundle.of(SslStoreBundle.of(keyStore, keystorePassword, trustStore));
    }

    private SslBundle loadSslBundle(KeyStore trustStore, KeyStore keyStore, String keystorePassword, String alias) {
        SslBundleKey bundleKey = SslBundleKey.of(keystorePassword, alias);
        return SslBundle.of(SslStoreBundle.of(keyStore, keystorePassword, trustStore), bundleKey);
    }

}
