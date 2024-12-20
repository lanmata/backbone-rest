/*
 * @(#)$file.className.java.
 *
 * Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *
 * All rights to this product are owned by Luis Antonio Mata Mata and may only
 * be used under the terms of its associated license document. You may NOT
 * copy, modify, sublicense, or distribute this source file or portions of
 * it unless previously authorized in writing by Luis Antonio Mata Mata.
 * In any event, this notice and the above copyright must always be included
 * verbatim with this file.
 */

package com.prx.backoffice.v1.users.api.to;

import com.prx.commons.pojo.Person;
import com.prx.commons.pojo.Role;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * UserTO.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 17-05-2022
 * @since 11
 */
public class UserTO {
    private UUID id;
    private String alias;
    private String password;
    private LocalDateTime createdDate;
    private LocalDateTime lastUpdate;
    private boolean active;
    private Person person;
    private Set<Role> roles;
    private UUID serviceId;

    /**
     * Default Constructor.
     */
    public UserTO() {
        //Default Constructor.
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public void setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
    }

    @Override
    public String toString() {
        return "UserTO{" +
                "id=" + id +
                ", alias='" + alias + '\'' +
                ", password='" + password + '\'' +
                ", active=" + active +
                ", person=" + person +
                ", roles=" + roles +
                ", serviceId=" + serviceId +
                '}';
    }
}
