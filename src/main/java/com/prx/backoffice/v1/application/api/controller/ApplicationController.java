package com.prx.backoffice.v1.application.api.controller;

import com.prx.backoffice.v1.application.Service;
import com.prx.backoffice.v1.application.api.to.ApplicationCreateRequest;
import com.prx.backoffice.v1.application.service.ApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("v1/services")
public class ApplicationController implements ApplicationApi {

    private final ApplicationService applicationService;


    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Override
    public ResponseEntity<Service> create(ApplicationCreateRequest applicationCreateRequest) {
        if(Objects.nonNull(applicationCreateRequest.getService())) {
            return applicationService.create(applicationCreateRequest.getService());
        }
        return ResponseEntity.badRequest().build();
    }
}
