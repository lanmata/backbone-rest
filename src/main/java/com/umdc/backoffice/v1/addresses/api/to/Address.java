/*
 *  @(#)Address.java
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
package com.umdc.backoffice.v1.addresses.api.to;

import java.util.UUID;

/**
 * Data transfer object representing an address, matching the {@code Address}
 * schema declared in {@code api.yaml}.
 */
public class Address {

    private UUID id;
    private UUID personId;
    private String content;
    private String zipcode;

    /**
     * Default constructor.
     */
    public Address() {
        // Default constructor
    }

    /**
     * Gets the address ID.
     *
     * @return the address ID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Sets the address ID.
     *
     * @param id the address ID to set
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Gets the ID of the person this address belongs to.
     *
     * @return the person ID
     */
    public UUID getPersonId() {
        return personId;
    }

    /**
     * Sets the ID of the person this address belongs to.
     *
     * @param personId the person ID to set
     */
    public void setPersonId(UUID personId) {
        this.personId = personId;
    }

    /**
     * Gets the address text.
     *
     * @return the address text
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the address text.
     *
     * @param content the address text to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets the zip code.
     *
     * @return the zip code
     */
    public String getZipcode() {
        return zipcode;
    }

    /**
     * Sets the zip code.
     *
     * @param zipcode the zip code to set
     */
    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }
}
