package com.umdc.backoffice.property;

/**
 * Properties for management authenticator configuration.
 * This class holds the configuration properties for the management authenticator,
 * including key alias, keystore, truststore, and MCAM token / rotation settings.
 */
public class ManagementAuthenticatorProperties {
    private String keyAlias;
    private StoreProperties keystore;
    private StoreProperties truststore;

    /**
     * M2M access token TTL in seconds. Defaults to 3600 (1 hour).
     */
    private long tokenTtlSeconds;

    /**
     * Grace period in seconds during which the old secret remains valid after rotation.
     */
    private long rotationGracePeriodSeconds;

    /**
     * Maximum token issuance requests per minute per clientId (sliding window).
     */
    private int rateLimitRpm;

    /**
     * Default constructor.
     * Creates a new instance of ManagementAuthenticatorProperties.
     */
    public ManagementAuthenticatorProperties() {
        // Default constructor
    }

    /**
     * Gets the key alias.
     *
     * @return the key alias
     */
    public String getKeyAlias() {
        return keyAlias;
    }

    /**
     * Sets the key alias.
     *
     * @param keyAlias the key alias to set
     */
    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    /**
     * Gets the keystore properties.
     *
     * @return the keystore properties
     */
    public StoreProperties getKeystore() {
        return keystore;
    }

    /**
     * Sets the keystore properties.
     *
     * @param keystore the keystore properties to set
     */
    public void setKeystore(StoreProperties keystore) {
        this.keystore = keystore;
    }

    /**
     * Gets the truststore properties.
     *
     * @return the truststore properties
     */
    public StoreProperties getTruststore() {
        return truststore;
    }

    /**
     * Sets the truststore properties.
     *
     * @param truststore the truststore properties to set
     */
    public void setTruststore(StoreProperties truststore) {
        this.truststore = truststore;
    }

    /**
     * Gets the M2M token TTL in seconds.
     *
     * @return the tokenTtlSeconds
     */
    public long getTokenTtlSeconds() {
        return tokenTtlSeconds;
    }

    /**
     * Sets the M2M token TTL in seconds.
     *
     * @param tokenTtlSeconds the tokenTtlSeconds to set
     */
    public void setTokenTtlSeconds(long tokenTtlSeconds) {
        this.tokenTtlSeconds = tokenTtlSeconds;
    }

    /**
     * Gets the rotation grace period in seconds.
     *
     * @return the rotationGracePeriodSeconds
     */
    public long getRotationGracePeriodSeconds() {
        return rotationGracePeriodSeconds;
    }

    /**
     * Sets the rotation grace period in seconds.
     *
     * @param rotationGracePeriodSeconds the rotationGracePeriodSeconds to set
     */
    public void setRotationGracePeriodSeconds(long rotationGracePeriodSeconds) {
        this.rotationGracePeriodSeconds = rotationGracePeriodSeconds;
    }

    /**
     * Gets the rate limit in requests per minute.
     *
     * @return the rateLimitRpm
     */
    public int getRateLimitRpm() {
        return rateLimitRpm;
    }

    /**
     * Sets the rate limit in requests per minute.
     *
     * @param rateLimitRpm the rateLimitRpm to set
     */
    public void setRateLimitRpm(int rateLimitRpm) {
        this.rateLimitRpm = rateLimitRpm;
    }
}
