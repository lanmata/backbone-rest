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

import java.util.Set;

/**
 * UserTO.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 17-05-2022
 * @since 11
 */
public class UserTO {
    private String id;
    private String alias;
    private String password;
    private boolean active;
    private Person person;
    private Set<Long> roles;

    /**
     * Default Constructor.
     */
    public UserTO() {
        //Default Constructor.
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public Set<Long> getRoles() {
        return roles;
    }

    public void setRoles(Set<Long> roles) {
        this.roles = roles;
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
                '}';
    }
}
