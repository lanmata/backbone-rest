/*
 *  @(#)IdentificationDocumentRequest.java
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

import com.umdc.commons.general.to.Request;

/**
 * Data transfer object for identification document create/update operations.
 * Extends the base {@link Request} envelope carrying {@code dateTime},
 * {@code appName} and {@code appToken}.
 */
public class IdentificationDocumentRequest extends Request {

    private IdentificationDocument identificationDocument;

    /**
     * Default constructor.
     */
    public IdentificationDocumentRequest() {
        super();
    }

    /**
     * Gets the identification document.
     *
     * @return the identification document
     */
    public IdentificationDocument getIdentificationDocument() {
        return identificationDocument;
    }

    /**
     * Sets the identification document.
     *
     * @param identificationDocument the identification document to set
     */
    public void setIdentificationDocument(IdentificationDocument identificationDocument) {
        this.identificationDocument = identificationDocument;
    }
}
