package com.prx.backoffice.v1.application.api.controller;

import com.prx.backoffice.v1.application.Service;
import com.prx.backoffice.v1.application.api.to.ApplicationCreateRequest;
import com.prx.backoffice.v1.application.service.ApplicationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name="service", description="The service API")
public interface ApplicationApi {

    default ApplicationService getService() {
        return new ApplicationService() {};
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<Service> create(@RequestBody ApplicationCreateRequest applicationCreateRequest) {
        return this.getService().create(applicationCreateRequest.getService());
    }


}
