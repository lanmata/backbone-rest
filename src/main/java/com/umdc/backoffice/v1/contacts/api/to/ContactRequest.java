/*
 *  @(#)ContactRequest.java
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

package com.umdc.backoffice.v1.contacts.api.to;


import com.prx.commons.general.pojo.Contact;
import com.prx.commons.general.to.Request;

import java.io.Serializable;

/**
 * ContactRequest.
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 * @version 1.0.0, 10-04-2022
 * @since 11
 */
public class ContactRequest extends Request implements Serializable {
    private Contact contact;

    /**
     * Default Constructor
     */
    public ContactRequest() {
        super();
        // Default Constructor
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    @Override
    public String toString() {
        return "ContactRequest{" +
                "contact=" + contact +
                '}';
    }
}
