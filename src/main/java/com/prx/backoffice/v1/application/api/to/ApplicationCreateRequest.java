package com.prx.backoffice.v1.application.api.to;

import com.prx.backoffice.v1.application.Service;
import com.prx.commons.to.Request;

public class ApplicationCreateRequest extends Request {
    private Service service;

    public ApplicationCreateRequest() {
        // Default constructor
        super();
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

}
