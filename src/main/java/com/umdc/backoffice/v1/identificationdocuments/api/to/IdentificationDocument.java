/*
 *  @(#)IdentificationDocument.java
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
package com.umdc.backoffice.v1.identificationdocuments.api.to;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Data transfer object representing a per-person identification document
 * instance, matching the {@code IdentificationDocument} schema declared in
 * {@code api.yaml}: a specific document number issued to a specific person.
 */
public class IdentificationDocument {

    private UUID id;
    private String number;
    private LocalDate expirationDate;
    private int identificationType;
    private UUID personId;

    /**
     * Default constructor.
     */
    public IdentificationDocument() {
        // Default constructor
    }

    /**
     * Gets the identifier.
     *
     * @return the identifier
     */
    public UUID getId() {
        return id;
    }

    /**
     * Sets the identifier.
     *
     * @param id the identifier to set
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Gets the document number.
     *
     * @return the document number
     */
    public String getNumber() {
        return number;
    }

    /**
     * Sets the document number.
     *
     * @param number the document number to set
     */
    public void setNumber(String number) {
        this.number = number;
    }

    /**
     * Gets the expiration date.
     *
     * @return the expiration date
     */
    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    /**
     * Sets the expiration date.
     *
     * @param expirationDate the expiration date to set
     */
    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    /**
     * Gets the identification type code (0 = PASSPORT, 1 = IDENTIFICATION).
     *
     * @return the identification type code
     */
    public int getIdentificationType() {
        return identificationType;
    }

    /**
     * Sets the identification type code (0 = PASSPORT, 1 = IDENTIFICATION).
     *
     * @param identificationType the identification type code to set
     */
    public void setIdentificationType(int identificationType) {
        this.identificationType = identificationType;
    }

    /**
     * Gets the owning person's identifier.
     *
     * @return the owning person's identifier
     */
    public UUID getPersonId() {
        return personId;
    }

    /**
     * Sets the owning person's identifier.
     *
     * @param personId the owning person's identifier to set
     */
    public void setPersonId(UUID personId) {
        this.personId = personId;
    }
}
