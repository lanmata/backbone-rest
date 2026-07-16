/*
 *  @(#)AddressRequest.java
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

import com.umdc.commons.general.to.Request;

/**
 * Data transfer object for address operations.
 * Extends the base Request class.
 */
public class AddressRequest extends Request {

    private Address address;

    /**
     * Default constructor.
     */
    public AddressRequest() {
        super();
    }

    /**
     * Gets the address.
     *
     * @return the address
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Sets the address.
     *
     * @param address the address to set
     */
    public void setAddress(Address address) {
        this.address = address;
    }
}
