package com.umdc.backoffice.util;

import com.umdc.backoffice.property.SecurityProperties;
import com.umdc.backoffice.property.StoreProperties;
import com.umdc.backoffice.security.exception.CertificateSecurityException;
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

/// Utility class for handling keystore operations.
/// This class provides methods for loading the keystore, printing the certificates in the keystore, and getting the SSL context.
///
/// @version 1.0
/// @since 1.0
@Component
public final class KeystoreUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeystoreUtil.class);

    /// Default constructor.
    public KeystoreUtil() {
        /* Default constructor for the KeyStoreUtil class. */
        LOGGER.info("KeyStoreUtil class instantiated.");
    }

    /// Get the keystore. Load the keystore from the location specified in the security properties.
    ///
    /// @param securityProperties the security properties
    /// @return the keystore
    /// @throws CertificateSecurityException if an error occurs while loading the keystore
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

    /// Print the certificates in the keystore. Log the certificates in the keystore.
    ///
    /// @param keystore     the keystore
    /// @param filename     the filename
    /// @param isTrustStore the is trust store
    /// @throws CertificateSecurityException if an error occurs while printing the certificates
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

    /// Get the SSL context. Load the SSL context from the security properties.
    /// This method is used to create the SSL context for the server.
    ///
    /// @param securityProperties the security properties
    /// @return the SSL context
    /// @throws NoSuchAlgorithmException     if the algorithm is not found
    /// @throws CertificateSecurityException if an error occurs while loading the keystore
    /// @throws UnrecoverableKeyException    if the key is unrecoverable
    /// @throws KeyStoreException            if an error occurs while loading the keystore
    /// @throws KeyManagementException       if an error occurs while loading the keystore
    /// @throws CertificateSecurityException if an error occurs while loading the keystore
    public SSLContext getSSLContext(SecurityProperties securityProperties) throws NoSuchAlgorithmException, CertificateSecurityException, UnrecoverableKeyException, KeyStoreException, KeyManagementException {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

        keyManagerFactory.init(getKeyStore(securityProperties.getKeystore()), securityProperties.getKeystore().getPassword().toCharArray());
        trustManagerFactory.init(getKeyStore(securityProperties.getTruststore()));

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
        return sslContext;
    }

    /// Get the SSL context. Load the SSL context from the security properties.
    /// This method is used to create the SSL context for the management authenticator.
    ///
    /// @param securityProperties the security properties
    /// @return the SSL context
    /// @throws CertificateSecurityException if an error occurs while loading the keystore
    public SslBundle getSslBundle(SecurityProperties securityProperties) throws CertificateSecurityException {
        return loadSslBundle(getKeyStore(securityProperties.getKeystore()),
                getKeyStore(securityProperties.getTruststore()),
                securityProperties.getKeystore().getPassword());
    }

    /// Get the SSL context. Load the SSL context from the security properties.
    /// This method is used to create the SSL context for the management authenticator.
    ///
    /// @param securityProperties the security properties
    /// @return the SSL context
    /// @throws CertificateSecurityException if an error occurs while loading the keystore
    public SslBundle getManagementAuthenticatorSslBundle(SecurityProperties securityProperties) throws CertificateSecurityException {
        return loadSslBundle(getKeyStore(securityProperties.getManagementAuthenticator().getKeystore()),
                getKeyStore(securityProperties.getManagementAuthenticator().getTruststore()),
                securityProperties.getManagementAuthenticator().getKeystore().getPassword(),
                securityProperties.getManagementAuthenticator().getKeyAlias());
    }

    /// Loads the {@link PrivateKey} for the given alias from the keystore described by {@code storeProperties}.
    ///
    /// @param storeProperties the keystore location/type/password descriptor
    /// @param alias           the key alias to retrieve
    /// @return the {@link PrivateKey} associated with the alias
    /// @throws CertificateSecurityException if the key cannot be loaded
    public PrivateKey loadPrivateKey(StoreProperties storeProperties, String alias)
            throws CertificateSecurityException {
        try {
            KeyStore ks = getKeyStore(storeProperties);
            return (PrivateKey) ks.getKey(alias, storeProperties.getPassword().toCharArray());
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            LOGGER.warn("Error loading private key for alias '{}'", alias);
            throw new CertificateSecurityException(e);
        }
    }

    /// Loads the {@link PublicKey} for the given alias from the keystore described by {@code storeProperties}.
    ///
    /// @param storeProperties the keystore location/type/password descriptor
    /// @param alias           the certificate alias to retrieve the public key from
    /// @return the {@link PublicKey} associated with the alias
    /// @throws CertificateSecurityException if the key cannot be loaded
    public PublicKey loadPublicKey(StoreProperties storeProperties, String alias)
            throws CertificateSecurityException {
        try {
            KeyStore ks = getKeyStore(storeProperties);
            Certificate cert = ks.getCertificate(alias);
            return cert.getPublicKey();
        } catch (KeyStoreException e) {
            LOGGER.warn("Error loading public key for alias '{}'", alias);
            throw new CertificateSecurityException(e);
        }
    }

    private SslBundle loadSslBundle(KeyStore trustStore, KeyStore keyStore, String keystorePassword) {
        return SslBundle.of(SslStoreBundle.of(keyStore, keystorePassword, trustStore));
    }

    private SslBundle loadSslBundle(KeyStore trustStore, KeyStore keyStore, String keystorePassword, String alias) {
        SslBundleKey bundleKey = SslBundleKey.of(keystorePassword, alias);
        return SslBundle.of(SslStoreBundle.of(keyStore, keystorePassword, trustStore), bundleKey);
    }

}
