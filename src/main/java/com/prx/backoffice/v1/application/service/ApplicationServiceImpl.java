package com.prx.backoffice.v1.application.service;

import com.prx.backoffice.v1.application.Service;
import com.prx.backoffice.v1.application.mapper.ApplicationMapper;
import com.prx.persistence.general.repositories.ServiceRepository;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
public class ApplicationServiceImpl implements ApplicationService {

    private final ServiceRepository serviceRepository;
    private final ApplicationMapper applicationMapper;

    public ApplicationServiceImpl(ServiceRepository serviceRepository, ApplicationMapper applicationMapper) {
        this.serviceRepository = serviceRepository;
        this.applicationMapper = applicationMapper;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Service> create(Service service) {
        var result = Optional.of(serviceRepository.save(applicationMapper.toSource(service)));
        return result.map(entity -> ResponseEntity.ok(applicationMapper.toTarget(entity))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Service> find(String id) {
        return ApplicationService.super.find(id);
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Service> update(String id, Service service) {
        return ApplicationService.super.update(id, service);
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Service> delete(String id, Service service) {
        return ApplicationService.super.delete(id, service);
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Service>> list(String... id) {
        return ApplicationService.super.list(id);
    }
}
