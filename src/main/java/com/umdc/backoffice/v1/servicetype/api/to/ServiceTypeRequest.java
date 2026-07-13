/*
 *  @(#)ServiceTypeRequest.java
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
package com.umdc.backoffice.v1.servicetype.api.to;

import com.umdc.commons.general.pojo.ServiceType;
import com.umdc.commons.general.to.Request;

/// Data transfer object for service type operations.
/// Extends the base Request class.
public class ServiceTypeRequest extends Request {

    private ServiceType serviceType;

    /// Default constructor.
    public ServiceTypeRequest() {
        super();
    }

    /// Gets the service type.
    ///
    /// @return the service type
    public ServiceType getServiceType() {
        return serviceType;
    }

    /// Sets the service type.
    ///
    /// @param serviceType the service type to set
    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }
}
