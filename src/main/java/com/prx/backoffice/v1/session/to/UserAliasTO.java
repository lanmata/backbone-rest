package com.prx.backoffice.v1.session.to;

import java.util.Set;
import java.util.UUID;

/**
 * A class representing a user's alias information.
 */
public class UserAliasTO {
    private UUID userId;
    private String alias;
    private String firstname;
    private String lastname;
    private Set<UUID> roles;

    public UserAliasTO() {
        // Default constructor
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    /**
     * Gets the user ID.
     *
     * @return the user ID
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * Sets the user ID.
     *
     * @param userId the user ID to set
     */
    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    /**
     * Gets the alias.
     *
     * @return the alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the alias.
     *
     * @param alias the alias to set
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Gets the role ID.
     *
     * @return the role ID
     */
    public Set<UUID> getRoles() {
        return roles;
    }

    /**
     * Sets the role ID.
     *
     * @param roles the role ID to set
     */
    public void setRoles(Set<UUID> roles) {
        this.roles = roles;
    }
}
