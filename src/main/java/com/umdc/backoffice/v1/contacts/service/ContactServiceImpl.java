/*
 *  @(#)ContactServiceImpl.java
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
package com.umdc.backoffice.v1.contacts.service;

import com.umdc.backoffice.v1.application.service.ApplicationGraphLookupService;
import com.umdc.backoffice.v1.contacts.api.to.ContactCreateRequest;
import com.umdc.backoffice.v1.contacts.mapper.ContactMapper;
import com.umdc.backoffice.v1.contacttypes.mapper.ContactTypeMapper;
import com.umdc.backoffice.v1.people.service.PersonGraphLookupService;
import com.umdc.commons.general.pojo.Contact;
import com.umdc.commons.general.pojo.ContactType;
import com.umdc.persistence.general.domains.ApplicationEntity;
import com.umdc.persistence.general.domains.ContactEntity;
import com.umdc.persistence.general.domains.ContactTypeEntity;
import com.umdc.persistence.general.repositories.ContactRepository;
import com.umdc.persistence.general.repositories.ContactTypeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import static com.umdc.backoffice.util.MessageUtil.MESSAGE_HEADER_STR;

/**
 * ContactService.
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 * @version 1.0.0, 20-10-2020
 */
@Service
public class ContactServiceImpl implements ContactService {

    private static final String BAD_REQUEST_MSG =
            "Invalid request. The 'contact' body is required and must include a non-null 'personId'.";
    private static final String PERSON_NOT_FOUND_MSG = "Person not found.";
    private static final String CONTACT_TYPE_NOT_FOUND_MSG = "Contact type not found.";
    private static final String APPLICATION_NOT_FOUND_MSG = "Application not found.";
    private static final String DUPLICATE_CONTACT_MSG =
            "This person already has a contact with the same value (phone number, email or messaging "
                    + "account) for this application.";

    private final ContactRepository contactRepository;
    private final ContactGraphLookupService contactGraphLookupService;
    private final PersonGraphLookupService personGraphLookupService;
    private final ApplicationGraphLookupService applicationGraphLookupService;
    private final ContactTypeRepository contactTypeRepository;
    private final ContactMapper contactMapper;
    private final ContactTypeMapper contactTypeMapper;
    @Value("${app.environments.contact.limit}")
    private int contactLimit;

    public ContactServiceImpl(ContactRepository contactRepository, ContactGraphLookupService contactGraphLookupService,
                               PersonGraphLookupService personGraphLookupService,
                               ApplicationGraphLookupService applicationGraphLookupService,
                               ContactTypeRepository contactTypeRepository,
                               ContactMapper contactMapper, ContactTypeMapper contactTypeMapper) {
        this.contactRepository = contactRepository;
        this.contactGraphLookupService = contactGraphLookupService;
        this.personGraphLookupService = personGraphLookupService;
        this.applicationGraphLookupService = applicationGraphLookupService;
        this.contactTypeRepository = contactTypeRepository;
        this.contactMapper = contactMapper;
        this.contactTypeMapper = contactTypeMapper;
    }

    @Override
    public List<Contact> saveAll(List<Contact> contacts) {
        final List<ContactEntity> results = new ArrayList<>();
        contacts.forEach(contact -> results.add(contactRepository.save(contactMapper.toSource(contact))));

        if (results.isEmpty()) {
            return new ArrayList<>();
        }
        return results.stream().map(contactMapper::toTarget).toList();
    }

    @Override
    public ResponseEntity<Contact> create(Contact contact) {
        if (null == contact || null == contact.getPersonId()) {
            return ResponseEntity.badRequest().header(MESSAGE_HEADER_STR, BAD_REQUEST_MSG).build();
        }
        return create(contact.getPersonId(), contact);
    }

    @Override
    public ResponseEntity<Contact> create(ContactCreateRequest request) {
        if (null == request || null == request.personId()) {
            return ResponseEntity.badRequest().header(MESSAGE_HEADER_STR, BAD_REQUEST_MSG).build();
        }
        ContactType contactType = null;
        if (Objects.nonNull(request.contentTypeId())) {
            Optional<ContactTypeEntity> contactTypeEntity = contactTypeRepository.findById(request.contentTypeId());
            if (contactTypeEntity.isEmpty()) {
                return ResponseEntity.badRequest().header(MESSAGE_HEADER_STR, CONTACT_TYPE_NOT_FOUND_MSG).build();
            }
            contactType = contactTypeMapper.toTarget(contactTypeEntity.get());
        }
        return create(request.personId(), toContact(request, contactType));
    }

    /**
     * Builds a {@link Contact} from the flat wire request — the real {@code Contact} DTO has no
     * flat contact-type-id field of its own, only a nested {@link ContactType} object, so the
     * resolved {@code contactType} (already looked up by the caller) is attached here.
     */
    private Contact toContact(ContactCreateRequest request, ContactType contactType) {
        Contact contact = new Contact();
        contact.setId(request.id());
        contact.setContent(request.content());
        contact.setPersonId(request.personId());
        contact.setApplicationId(request.applicationId());
        contact.setActive(request.active());
        contact.setContactType(contactType);
        return contact;
    }

    @Override
    public ResponseEntity<Contact> create(UUID personId, Contact contact) {
        if (null == contact) {
            return ResponseEntity.badRequest().header(MESSAGE_HEADER_STR, BAD_REQUEST_MSG).build();
        }
        var person = personGraphLookupService.findByIdSafe(personId);
        if (person.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).header(MESSAGE_HEADER_STR, PERSON_NOT_FOUND_MSG).build();
        }
        ApplicationEntity application = null;
        if (Objects.nonNull(contact.getApplicationId())) {
            var applicationResult = applicationGraphLookupService.findByIdSafe(contact.getApplicationId());
            if (applicationResult.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).header(MESSAGE_HEADER_STR, APPLICATION_NOT_FOUND_MSG).build();
            }
            application = applicationResult.get();
        }
        List<ContactEntity> existingContacts = contactGraphLookupService.findByPersonIdWithGraph(personId);
        if (isDuplicateContact(existingContacts, contact.getApplicationId(), contact.getContent(), null)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).header(MESSAGE_HEADER_STR, DUPLICATE_CONTACT_MSG).build();
        }
        if (existingContacts.size() >= contactLimit) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).header(MESSAGE_HEADER_STR, "Contact NOT created. Contact limit has been reached.").build();
        }
        // ContactEntity.id is @GeneratedValue(strategy = IDENTITY) (DB-generated) — a
        // client-supplied id here makes Spring Data's save() treat this as an update
        // (entityInformation.isNew() sees a non-null id) and route to merge() instead of
        // persist(), which throws StaleObjectStateException/ObjectOptimisticLockingFailure
        // since no row with that id actually exists yet. Clearing it forces a genuine
        // insert. Same root cause already fixed for PersonServiceImpl#create /
        // AddressServiceImpl#create.
        contact.setId(null);
        // ContactMapper#toSource deliberately ignores "person" (a flat personId can't be
        // auto-mapped to the full entity reference MapStruct needs) — it must be set here
        // explicitly, otherwise the saved row has a null person_id and becomes permanently
        // invisible to listByPersonId's "WHERE ce.person.id = :personId" query. The same
        // name mismatch (applicationId UUID vs. application entity reference) means
        // "application" needs the identical explicit treatment.
        var contactEntity = contactMapper.toSource(contact);
        contactEntity.setPerson(person.get());
        contactEntity.setApplication(application);
        var response = contactRepository.save(contactEntity);
        return ResponseEntity.status(HttpStatus.CREATED).header(MESSAGE_HEADER_STR, "Contact created").body(contactMapper.toTarget(response));
    }

    /**
     * A person cannot have two contacts with the same value (phone number, email, messaging
     * account, etc.) within the same application — checked case-insensitively, trimmed, against
     * every other contact already linked to that person for that application.
     *
     * @param existingContacts  the person's current contacts (from {@link ContactGraphLookupService})
     * @param applicationId     the application the new/updated contact belongs to
     * @param content           the candidate content value
     * @param excludeContactId  on update, the id of the contact being updated (never flagged as a
     *                          duplicate of itself); {@code null} on create
     * @return {@code true} if another contact for the same person and application already has this
     *         exact content
     */
    private boolean isDuplicateContact(List<ContactEntity> existingContacts, UUID applicationId, String content,
                                        UUID excludeContactId) {
        if (Objects.isNull(content)) {
            return false;
        }
        String normalizedContent = content.trim();
        return existingContacts.stream()
                .filter(existing -> !existing.getId().equals(excludeContactId))
                .filter(existing -> sameApplication(existing.getApplication(), applicationId))
                .anyMatch(existing -> normalizedContent.equalsIgnoreCase(existing.getContent()));
    }

    private boolean sameApplication(ApplicationEntity existingApplication, UUID applicationId) {
        UUID existingApplicationId = Objects.isNull(existingApplication) ? null : existingApplication.getId();
        return Objects.equals(existingApplicationId, applicationId);
    }

    @Override
    public ResponseEntity<Contact> update(UUID contactId, Contact contact) {
        if (null == contactId || null == contact) {
            return ResponseEntity.notFound().build();
        }
        var contactOptionResult = contactGraphLookupService.findByIdWithGraph(contactId);
        if (contactOptionResult.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var contactEntity = contactOptionResult.get();
        UUID personId = Objects.isNull(contactEntity.getPerson()) ? null : contactEntity.getPerson().getId();
        UUID applicationId = Objects.isNull(contactEntity.getApplication()) ? null : contactEntity.getApplication().getId();
        if (Objects.nonNull(personId)) {
            List<ContactEntity> existingContacts = contactGraphLookupService.findByPersonIdWithGraph(personId);
            if (isDuplicateContact(existingContacts, applicationId, contact.getContent(), contactId)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).header(MESSAGE_HEADER_STR, DUPLICATE_CONTACT_MSG).build();
            }
        }
        contactEntity.setContent(contact.getContent());
        contactEntity.setActive(contact.getActive());
        contactEntity.setContactType(contactTypeMapper.toSource(contact.getContactType()));
        return ResponseEntity.ok(contactMapper.toTarget(contactRepository.save(contactEntity)));
    }

    @Override
    public ResponseEntity<Contact> update(UUID contactId, ContactCreateRequest request) {
        if (null == contactId || null == request) {
            return ResponseEntity.notFound().build();
        }
        ContactType contactType = null;
        if (Objects.nonNull(request.contentTypeId())) {
            Optional<ContactTypeEntity> contactTypeEntity = contactTypeRepository.findById(request.contentTypeId());
            if (contactTypeEntity.isEmpty()) {
                return ResponseEntity.badRequest().header(MESSAGE_HEADER_STR, CONTACT_TYPE_NOT_FOUND_MSG).build();
            }
            contactType = contactTypeMapper.toTarget(contactTypeEntity.get());
        }
        return update(contactId, toContact(request, contactType));
    }

    @Override
    public ResponseEntity<Contact> find(UUID contactId) {
        if (null == contactId) {
            return ResponseEntity.badRequest().build();
        }
        var contactEntityResult = contactGraphLookupService.findByIdWithGraph(contactId);
        if (contactEntityResult.isPresent()) {
            var contact = contactMapper.toTarget(contactEntityResult.get());
            return ResponseEntity.ok(contact);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<List<Contact>> listByPersonId(UUID personId) {
        // Always 200 (even with an empty body) rather than 404 when a person simply has no
        // contacts yet: create(UUID, Contact) below calls this method as its contact-limit
        // pre-check, and a person's very first contact would otherwise be silently rejected
        // (falling through to the 204 branch) because the "no contacts yet" case would read as
        // "lookup failed" instead of "lookup succeeded with zero results" — matching the
        // original ContactRepository#listByPersonId behavior, which never produced
        // Optional.empty() for a merely-empty (vs. absent) list.
        var contactEntities = contactGraphLookupService.findByPersonIdWithGraph(personId);
        var contactList = new ArrayList<Contact>();
        contactEntities.forEach(contactEntity -> contactList.add(contactMapper.toTarget(contactEntity)));
        return ResponseEntity.ok(contactList);
    }

    @Override
    public ResponseEntity<String> deleteById(UUID contactId) {
        var contactItem = contactGraphLookupService.findByIdWithGraph(contactId);
        if (contactItem.isPresent()) {
            contactRepository.deleteById(contactId);
            return ResponseEntity.accepted().header(MESSAGE_HEADER_STR, "The Contact has been removed.").build();
        }
        return ResponseEntity.notFound().header(MESSAGE_HEADER_STR, "The Contact is NOT present.").build();
    }
}
