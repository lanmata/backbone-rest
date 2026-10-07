/*
 *  @(#)KeystoreUtilTest.java
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

package com.umdc.backoffice.util;

import com.umdc.backoffice.property.ManagementAuthenticatorProperties;
import com.umdc.backoffice.property.SecurityProperties;
import com.umdc.backoffice.property.StoreProperties;
import com.umdc.backoffice.security.exception.CertificateSecurityException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;

/// Unit tests for [KeystoreUtil], backed by a real test-only JKS keystore
/// (`src/test/resources/keystoreutil-test.jks`, alias `test-key`, password `changeit`)
/// generated with `keytool` — resource loading can't be mocked without PowerMock, so this
/// exercises the class against a genuine keystore instead.
class KeystoreUtilTest {

    private static final String KEYSTORE_RESOURCE = "keystoreutil-test.jks";
    private static final String MISMATCHED_KEYPASS_KEYSTORE_RESOURCE = "keystoreutil-mismatched-keypass.jks";
    private static final String STORE_PASSWORD = System.getProperty("keystoreutil.test.storePassword", "changeit");
    private static final String INVALID_STORE_PASSWORD = java.util.UUID.randomUUID().toString();
    private static final String KEY_ALIAS = "test-key";
    private static final String KEY_ALIAS_WITH_MISMATCHED_KEYPASS = "test-key-badkeypass";

    private KeystoreUtil keystoreUtil;
    private StoreProperties storeProperties;

    @BeforeEach
    void setUp() {
        keystoreUtil = new KeystoreUtil();
        storeProperties = new StoreProperties();
        storeProperties.setLocation(KEYSTORE_RESOURCE);
        storeProperties.setType("JKS");
        storeProperties.setPassword(STORE_PASSWORD);
    }

    @Test
    @DisplayName("getKeyStore loads a real keystore resource from a plain (non classpath:) location")
    void getKeyStore_plainLocation_loadsSuccessfully() throws CertificateSecurityException {
        KeyStore keyStore = keystoreUtil.getKeyStore(storeProperties);

        Assertions.assertNotNull(keyStore);
    }

    @Test
    @DisplayName("getKeyStore strips the classpath: prefix and loads the same resource")
    void getKeyStore_classpathPrefixedLocation_loadsSuccessfully() throws CertificateSecurityException {
        storeProperties.setLocation("classpath:" + KEYSTORE_RESOURCE);

        KeyStore keyStore = keystoreUtil.getKeyStore(storeProperties);

        Assertions.assertNotNull(keyStore);
    }

    @Test
    @DisplayName("getKeyStore wraps a missing resource in CertificateSecurityException")
    void getKeyStore_missingResource_throwsCertificateSecurityException() {
        storeProperties.setLocation("does-not-exist.jks");

        Assertions.assertThrows(CertificateSecurityException.class,
                () -> keystoreUtil.getKeyStore(storeProperties));
    }

    @Test
    @DisplayName("getKeyStore wraps a wrong password in CertificateSecurityException")
    void getKeyStore_wrongPassword_throwsCertificateSecurityException() {
        storeProperties.setPassword(INVALID_STORE_PASSWORD);

        Assertions.assertThrows(CertificateSecurityException.class,
                () -> keystoreUtil.getKeyStore(storeProperties));
    }

    @Test
    @DisplayName("getKeyStore propagates a NullPointerException when the location is null")
    void getKeyStore_nullLocation_throwsNullPointerException() {
        storeProperties.setLocation(null);

        Assertions.assertThrows(NullPointerException.class,
                () -> keystoreUtil.getKeyStore(storeProperties));
    }

    @Test
    @DisplayName("getKeyStore wraps an unsupported store type in CertificateSecurityException")
    void getKeyStore_unsupportedType_throwsCertificateSecurityException() {
        storeProperties.setType("BOGUS-TYPE");

        Assertions.assertThrows(CertificateSecurityException.class,
                () -> keystoreUtil.getKeyStore(storeProperties));
    }

    @Test
    @DisplayName("certificatePrint logs every alias for a keystore (isTrustStore=false)")
    void certificatePrint_asKeystore_doesNotThrow() throws CertificateSecurityException {
        KeyStore keyStore = keystoreUtil.getKeyStore(storeProperties);

        Assertions.assertDoesNotThrow(() -> keystoreUtil.certificatePrint(keyStore, KEYSTORE_RESOURCE, false));
    }

    @Test
    @DisplayName("certificatePrint logs every alias for a truststore (isTrustStore=true)")
    void certificatePrint_asTruststore_doesNotThrow() throws CertificateSecurityException {
        KeyStore keyStore = keystoreUtil.getKeyStore(storeProperties);

        Assertions.assertDoesNotThrow(() -> keystoreUtil.certificatePrint(keyStore, KEYSTORE_RESOURCE, true));
    }

    @Test
    @DisplayName("certificatePrint wraps a KeyStoreException from an uninitialized keystore")
    void certificatePrint_uninitializedKeystore_throwsCertificateSecurityException() throws KeyStoreException {
        KeyStore uninitialized = KeyStore.getInstance("JKS");

        Assertions.assertThrows(CertificateSecurityException.class,
                () -> keystoreUtil.certificatePrint(uninitialized, KEYSTORE_RESOURCE, false));
    }

    @Test
    @DisplayName("getSSLContext builds a TLS context from matching keystore/truststore properties")
    void getSSLContext_buildsContext() throws Exception {
        var securityProperties = securityPropertiesWithSameStoreForBoth();

        var sslContext = keystoreUtil.getSSLContext(securityProperties);

        Assertions.assertNotNull(sslContext);
        Assertions.assertEquals("TLS", sslContext.getProtocol());
    }

    @Test
    @DisplayName("getSslBundle builds an SslBundle from matching keystore/truststore properties")
    void getSslBundle_buildsBundle() throws CertificateSecurityException {
        var securityProperties = securityPropertiesWithSameStoreForBoth();

        var sslBundle = keystoreUtil.getSslBundle(securityProperties);

        Assertions.assertNotNull(sslBundle);
        Assertions.assertNotNull(sslBundle.getStores());
    }

    @Test
    @DisplayName("getManagementAuthenticatorSslBundle builds an SslBundle keyed to the mcam alias")
    void getManagementAuthenticatorSslBundle_buildsBundle() throws CertificateSecurityException {
        var securityProperties = new SecurityProperties();
        var mcam = new ManagementAuthenticatorProperties();
        mcam.setKeyAlias(KEY_ALIAS);
        mcam.setKeystore(storeProperties);
        mcam.setTruststore(storeProperties);
        securityProperties.setManagementAuthenticator(mcam);

        var sslBundle = keystoreUtil.getManagementAuthenticatorSslBundle(securityProperties);

        Assertions.assertNotNull(sslBundle);
        Assertions.assertEquals(KEY_ALIAS, sslBundle.getKey().getAlias());
    }

    @Test
    @DisplayName("loadPrivateKey returns the RSA private key for a known alias")
    void loadPrivateKey_knownAlias_returnsKey() throws CertificateSecurityException {
        PrivateKey privateKey = keystoreUtil.loadPrivateKey(storeProperties, KEY_ALIAS);

        Assertions.assertNotNull(privateKey);
        Assertions.assertEquals("RSA", privateKey.getAlgorithm());
    }

    @Test
    @DisplayName("loadPrivateKey wraps a CertificateSecurityException when the store itself won't open")
    void loadPrivateKey_storeLoadFails_throwsCertificateSecurityException() {
        storeProperties.setPassword(INVALID_STORE_PASSWORD);

        Assertions.assertThrows(CertificateSecurityException.class,
                () -> keystoreUtil.loadPrivateKey(storeProperties, KEY_ALIAS));
    }

    @Test
    @DisplayName("loadPrivateKey wraps an UnrecoverableKeyException when the entry's own key password differs from the store password")
    void loadPrivateKey_mismatchedKeyPassword_throwsCertificateSecurityException() {
        var mismatchedKeyPassStore = new StoreProperties();
        mismatchedKeyPassStore.setLocation(MISMATCHED_KEYPASS_KEYSTORE_RESOURCE);
        mismatchedKeyPassStore.setType("JKS");
        mismatchedKeyPassStore.setPassword(STORE_PASSWORD);

        Assertions.assertThrows(CertificateSecurityException.class,
                () -> keystoreUtil.loadPrivateKey(mismatchedKeyPassStore, KEY_ALIAS_WITH_MISMATCHED_KEYPASS));
    }

    @Test
    @DisplayName("loadPublicKey returns the certificate's public key for a known alias")
    void loadPublicKey_knownAlias_returnsKey() throws CertificateSecurityException {
        PublicKey publicKey = keystoreUtil.loadPublicKey(storeProperties, KEY_ALIAS);

        Assertions.assertNotNull(publicKey);
        Assertions.assertEquals("RSA", publicKey.getAlgorithm());
    }

    @Test
    @DisplayName("loadPublicKey throws when no certificate exists for the alias")
    void loadPublicKey_unknownAlias_throwsCertificateSecurityException() {
        Assertions.assertThrows(CertificateSecurityException.class,
                () -> keystoreUtil.loadPublicKey(storeProperties, "no-such-alias"));
    }

    private SecurityProperties securityPropertiesWithSameStoreForBoth() {
        var securityProperties = new SecurityProperties();
        securityProperties.setKeystore(storeProperties);
        securityProperties.setTruststore(storeProperties);
        return securityProperties;
    }
}
