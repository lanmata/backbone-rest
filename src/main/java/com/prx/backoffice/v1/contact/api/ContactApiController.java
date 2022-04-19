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

package com.prx.backoffice.v1.contact.api;

import com.prx.backoffice.v1.contact.to.ContactRequest;
import com.prx.commons.pojo.Contact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * ContactApiController.
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 * @version 1.0.0, 07-04-2022
 * @since 11
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("v1/contact")
public class ContactApiController implements ContactApi {

    private final ContactApiDelegate contactApiDelegate;

    public ContactApiController(@Autowired(required = false) ContactApiDelegate contactApiDelegate) {
        this.contactApiDelegate = Optional.ofNullable(contactApiDelegate).orElse(new ContactApiDelegate(){});
    }

    @Override
    public ResponseEntity<Contact> create(ContactRequest contactRequest) {
        return ContactApi.super.create(contactRequest);
    }

    @Override
    public ContactApiDelegate getDelegate() {
        return contactApiDelegate;
    }


}
