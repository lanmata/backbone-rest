/*
 *  @(#)IdentificationDocumentServiceImpl.java
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
package com.umdc.backoffice.v1.identificationdocuments.service;

import com.umdc.backoffice.v1.identificationdocuments.api.to.IdentificationDocument;
import com.umdc.commons.constants.types.IdentificationType;
import com.umdc.persistence.general.domains.IdentificationDocumentEntity;
import com.umdc.persistence.general.domains.PersonEntity;
import com.umdc.persistence.general.repositories.IdentificationDocumentRepository;
import com.umdc.persistence.general.repositories.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.umdc.backoffice.util.MessageUtil.MESSAGE_HEADER_STR;

/**
 * Service implementation for identification document operations.
 * <p>
 * Handles the explicit field-by-field conversions between the flat wire DTO
 * ({@link IdentificationDocument}) and {@link IdentificationDocumentEntity}
 * inline, since the two shapes diverge on {@code number} (String vs Integer),
 * {@code expirationDate} (LocalDate vs LocalDateTime) and
 * {@code identificationType} (ordinal integer vs enum) — a plain MapStruct
 * mapping would not add value here.
 * </p>
 */
@Service
public class IdentificationDocumentServiceImpl implements IdentificationDocumentService {

    private static final Logger log = LoggerFactory.getLogger(IdentificationDocumentServiceImpl.class);

    private static final String NOT_FOUND_MSG = "Identification document not found.";
    private static final String FOUND_MSG = "Identification document found.";
    private static final String CREATED_MSG = "Identification document created.";
    private static final String UPDATED_MSG = "Identification document updated.";
    private static final String DELETED_MSG = "Identification document deleted.";
    private static final String BAD_REQUEST_MSG = "Invalid request. The 'identificationDocument' body is required and "
            + "must include a well-formed 'number' and a valid 'identificationType' (0 or 1).";
    private static final String PERSON_NOT_FOUND_MSG = "Person not found.";
    private static final String NO_DOCUMENTS_MSG = "No identification documents found for the given person.";

    private final IdentificationDocumentRepository identificationDocumentRepository;
    private final PersonRepository personRepository;

    /**
     * Constructor for IdentificationDocumentServiceImpl.
     *
     * @param identificationDocumentRepository the identification document repository
     * @param personRepository                 the person repository
     */
    public IdentificationDocumentServiceImpl(IdentificationDocumentRepository identificationDocumentRepository,
                                              PersonRepository personRepository) {
        this.identificationDocumentRepository = identificationDocumentRepository;
        this.personRepository = personRepository;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public ResponseEntity<IdentificationDocument> create(IdentificationDocument identificationDocument) {
        if (Objects.isNull(identificationDocument) || Objects.isNull(identificationDocument.getPersonId())) {
            log.debug("create called with null identificationDocument or null personId");
            return ResponseEntity.badRequest().header(MESSAGE_HEADER_STR, BAD_REQUEST_MSG).build();
        }
        IdentificationType type = resolveIdentificationType(identificationDocument.getIdentificationType());
        Integer number = parseNumber(identificationDocument.getNumber());
        if (Objects.isNull(type) || Objects.isNull(number)) {
            log.debug("create called with invalid number or identificationType");
            return ResponseEntity.badRequest().header(MESSAGE_HEADER_STR, BAD_REQUEST_MSG).build();
        }
        if (Objects.isNull(identificationDocument.getExpirationDate())) {
            log.debug("create called with null expirationDate");
            return ResponseEntity.badRequest().header(MESSAGE_HEADER_STR, BAD_REQUEST_MSG).build();
        }
        Optional<PersonEntity> person = personRepository.findById(identificationDocument.getPersonId());
        if (person.isEmpty()) {
            log.debug("Person not found for create: personId={}", identificationDocument.getPersonId());
            return ResponseEntity.badRequest().header(MESSAGE_HEADER_STR, PERSON_NOT_FOUND_MSG).build();
        }
        IdentificationDocumentEntity entity = new IdentificationDocumentEntity();
        entity.setId(UUID.randomUUID());
        entity.setNumber(number.toString());
        entity.setExpirationDate(toStartOfDay(identificationDocument.getExpirationDate()).toLocalDate());
        entity.setIdentificationType(type);
        entity.setPerson(person.get());
        IdentificationDocumentEntity saved = identificationDocumentRepository.save(entity);
        log.debug("Identification document created: id={}", saved.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(MESSAGE_HEADER_STR, CREATED_MSG)
                .body(toDto(saved));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<IdentificationDocument> find(UUID id) {
        if (Objects.isNull(id)) {
            log.debug("find called with null id");
            return ResponseEntity.badRequest().header(MESSAGE_HEADER_STR, BAD_REQUEST_MSG).build();
        }
        Optional<IdentificationDocumentEntity> entity = identificationDocumentRepository.findById(id);
        return entity.map(e -> ResponseEntity.ok().header(MESSAGE_HEADER_STR, FOUND_MSG).body(toDto(e)))
                .orElseGet(() -> ResponseEntity.notFound().header(MESSAGE_HEADER_STR, NOT_FOUND_MSG).build());
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public ResponseEntity<IdentificationDocument> update(UUID id, IdentificationDocument identificationDocument) {
        if (Objects.isNull(id) || Objects.isNull(identificationDocument)) {
            log.debug("update called with null id or null identificationDocument");
            return ResponseEntity.badRequest().header(MESSAGE_HEADER_STR, BAD_REQUEST_MSG).build();
        }
        Optional<IdentificationDocumentEntity> existing = identificationDocumentRepository.findById(id);
        if (existing.isEmpty()) {
            log.debug("Identification document not found for update: id={}", id);
            return ResponseEntity.notFound().header(MESSAGE_HEADER_STR, NOT_FOUND_MSG).build();
        }
        IdentificationType type = resolveIdentificationType(identificationDocument.getIdentificationType());
        Integer number = parseNumber(identificationDocument.getNumber());
        if (Objects.isNull(type) || Objects.isNull(number)) {
            log.debug("update called with invalid number or identificationType: id={}", id);
            return ResponseEntity.badRequest().header(MESSAGE_HEADER_STR, BAD_REQUEST_MSG).build();
        }
        if (Objects.isNull(identificationDocument.getExpirationDate())) {
            log.debug("update called with null expirationDate: id={}", id);
            return ResponseEntity.badRequest().header(MESSAGE_HEADER_STR, BAD_REQUEST_MSG).build();
        }
        IdentificationDocumentEntity entity = existing.get();
        UUID requestedPersonId = identificationDocument.getPersonId();
        if (!Objects.isNull(requestedPersonId) && !requestedPersonId.equals(currentPersonId(entity))) {
            Optional<PersonEntity> person = personRepository.findById(requestedPersonId);
            if (person.isEmpty()) {
                log.debug("Person not found for update: personId={}", requestedPersonId);
                return ResponseEntity.badRequest().header(MESSAGE_HEADER_STR, PERSON_NOT_FOUND_MSG).build();
            }
            entity.setPerson(person.get());
        }
        entity.setNumber(number.toString());
        entity.setExpirationDate(toStartOfDay(identificationDocument.getExpirationDate()).toLocalDate());
        entity.setIdentificationType(type);
        IdentificationDocumentEntity saved = identificationDocumentRepository.save(entity);
        log.debug("Identification document updated: id={}", saved.getId());
        return ResponseEntity.ok().header(MESSAGE_HEADER_STR, UPDATED_MSG).body(toDto(saved));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public ResponseEntity<IdentificationDocument> delete(UUID id, IdentificationDocument identificationDocument) {
        Optional<IdentificationDocumentEntity> existing = identificationDocumentRepository.findById(id);
        if (existing.isEmpty()) {
            log.debug("Identification document not found for delete: id={}", id);
            return ResponseEntity.notFound().header(MESSAGE_HEADER_STR, NOT_FOUND_MSG).build();
        }
        IdentificationDocument deleted = toDto(existing.get());
        identificationDocumentRepository.deleteById(id);
        log.debug("Identification document deleted: id={}", id);
        return ResponseEntity.ok().header(MESSAGE_HEADER_STR, DELETED_MSG).body(deleted);
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<IdentificationDocument>> listByPerson(UUID personId) {
        if (Objects.isNull(personId)) {
            log.debug("listByPerson called with null personId");
            return ResponseEntity.badRequest().header(MESSAGE_HEADER_STR, BAD_REQUEST_MSG).build();
        }
        if (!personRepository.existsById(personId)) {
            log.debug("Person not found: personId={}", personId);
            return ResponseEntity.notFound().header(MESSAGE_HEADER_STR, PERSON_NOT_FOUND_MSG).build();
        }
        List<IdentificationDocument> result = new ArrayList<>();
        identificationDocumentRepository.findByPersonId(personId).forEach(entity -> result.add(toDto(entity)));
        if (result.isEmpty()) {
            log.debug("No identification documents found for person: personId={}", personId);
            return ResponseEntity.notFound().header(MESSAGE_HEADER_STR, NO_DOCUMENTS_MSG).build();
        }
        return ResponseEntity.ok().header(MESSAGE_HEADER_STR, FOUND_MSG).body(result);
    }

    /**
     * Converts an {@link IdentificationDocumentEntity} to the flat wire DTO.
     *
     * @param entity the entity to convert
     * @return the converted DTO
     */
    private IdentificationDocument toDto(IdentificationDocumentEntity entity) {
        IdentificationDocument dto = new IdentificationDocument();
        dto.setId(entity.getId());
        dto.setNumber(Objects.isNull(entity.getNumber()) ? null : String.valueOf(entity.getNumber()));
        dto.setExpirationDate(Objects.isNull(entity.getExpirationDate())
                ? null : entity.getExpirationDate());
        dto.setIdentificationType(Objects.isNull(entity.getIdentificationType())
                ? 0 : entity.getIdentificationType().ordinal());
        dto.setPersonId(currentPersonId(entity));
        return dto;
    }

    /**
     * Returns the owning person's identifier for the given entity, or {@code null} if unset.
     *
     * @param entity the identification document entity
     * @return the owning person's identifier, or {@code null}
     */
    private UUID currentPersonId(IdentificationDocumentEntity entity) {
        return Objects.isNull(entity.getPerson()) ? null : entity.getPerson().getId();
    }

    /**
     * Resolves the integer ordinal from the wire DTO into an {@link IdentificationType}.
     *
     * @param ordinal 0 for PASSPORT, 1 for IDENTIFICATION
     * @return the matching enum value, or {@code null} if the ordinal is out of range
     */
    private IdentificationType resolveIdentificationType(int ordinal) {
        IdentificationType[] values = IdentificationType.values();
        if (ordinal < 0 || ordinal >= values.length) {
            return null;
        }
        return values[ordinal];
    }

    /**
     * Parses the wire DTO's string document number into the entity's integer representation.
     *
     * @param number the string document number
     * @return the parsed integer, or {@code null} if blank or not a valid integer
     */
    private Integer parseNumber(String number) {
        if (Objects.isNull(number) || number.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(number.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Converts the wire DTO's {@link LocalDate} into the entity's start-of-day {@link LocalDateTime}.
     *
     * @param expirationDate the expiration date
     * @return the start-of-day date-time, or {@code null} if the input is {@code null}
     */
    private LocalDateTime toStartOfDay(LocalDate expirationDate) {
        return Objects.isNull(expirationDate) ? null : expirationDate.atStartOfDay();
    }
}
