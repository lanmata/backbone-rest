package com.umdc.backoffice.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for JWT converter.
 * This class holds the configuration properties for the JWT converter, including resource ID and principal claim name.
 */
@Component
@ConfigurationProperties(prefix = "umdc.auth.converter")
public class JwtConverterProperties {

    /**
     * The resource ID for the JWT converter.
     * This property holds the identifier for the resource that the JWT is intended for.
     */
    private String resourceId;

    /**
     * The principal claim name for the JWT converter.
     * This property holds the name of the claim that contains the principal information.
     */
    private String principalClaimName;

    /**
     * Default constructor.
     * Creates a new instance of JwtConverterProperties.
     */
    public JwtConverterProperties() {
        // Default constructor
    }

    /**
     * Gets the principal claim name.
     *
     * @return the principal claim name
     */
    public String getPrincipalClaimName() {
        return principalClaimName;
    }

    /**
     * Sets the principal claim name.
     *
     * @param principalClaimName the principal claim name to set
     */
    public void setPrincipalClaimName(String principalClaimName) {
        this.principalClaimName = principalClaimName;
    }

    /**
     * Gets the resource ID.
     *
     * @return the resource ID
     */
    public String getResourceId() {
        return resourceId;
    }

    /**
     * Sets the resource ID.
     *
     * @param resourceId the resource ID to set
     */
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
}
