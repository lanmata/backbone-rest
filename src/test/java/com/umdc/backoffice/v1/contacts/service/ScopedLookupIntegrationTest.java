package com.umdc.backoffice.v1.contacts.service;

import com.umdc.backoffice.v1.features.service.FeatureGraphLookupServiceImpl;
import com.umdc.persistence.general.domains.ApplicationEntity;
import com.umdc.persistence.general.domains.ContactEntity;
import com.umdc.persistence.general.domains.ContactTypeEntity;
import com.umdc.persistence.general.domains.FeatureEntity;
import com.umdc.persistence.general.domains.PersonEntity;
import com.umdc.persistence.general.domains.RoleEntity;
import com.umdc.persistence.general.domains.RoleFeatureEntity;
import com.umdc.persistence.general.domains.RoleFeaturePK;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.umdc.backoffice.constant.BackboneAppConstants.ENTITY_PACKAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs the dynamic JPQL of the application-scoped contact directory and the paginated feature catalog
 * against a real Hibernate session (H2, same hand-written schema as the user graph test) — the unit
 * tests mock {@code EntityManager}, so they cannot prove the queries are valid or correctly scoped.
 */
@DataJpaTest(excludeAutoConfiguration = DataJpaRepositoriesAutoConfiguration.class)
@ContextConfiguration(classes = ScopedLookupIntegrationTest.MinimalJpaTestConfig.class)
@TestPropertySource(properties = {
        "SPRING_BOOT_PROFILE_ACTIVE=test",
        "spring.datasource.url=jdbc:h2:mem:scopedLookupTest;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:user-graph-lookup-test-schema.sql"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan(basePackages = ENTITY_PACKAGE)
class ScopedLookupIntegrationTest {

    @SpringBootConfiguration
    static class MinimalJpaTestConfig {
    }

    @Autowired
    private TestEntityManager em;

    private ContactGraphLookupServiceImpl contacts;
    private FeatureGraphLookupServiceImpl features;

    private ApplicationEntity appA;
    private ApplicationEntity appB;
    private ContactTypeEntity email;
    private ContactTypeEntity phone;
    private PersonEntity person;
    private final UUID merchantId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        contacts = new ContactGraphLookupServiceImpl(em.getEntityManager());
        features = new FeatureGraphLookupServiceImpl(em.getEntityManager());
        appA = application("shop-index", "SHOP");
        appB = application("other-app", "OTHER");
        email = contactType("EMAIL");
        phone = contactType("PHONE");
        person = new PersonEntity();
        person.setName("Jane");
        person.setLastName("Doe");
        person.setGender("F");
        person.setBirthdate(LocalDate.of(1990, 1, 1));
        em.persist(person);
    }

    @Test
    @DisplayName("the directory only returns the contacts of the requested application")
    void search_isScopedToTheApplication() {
        contact("jane@a.com", email, true, appA, person, null, null);
        contact("+1-555-0100", phone, true, appA, person, null, null);
        contact("jane@b.com", email, true, appB, person, null, null);
        contact("shop@a.com", email, true, appA, null, "MERCHANT", merchantId);
        contact("orphan@b.com", email, true, appB, null, "MERCHANT", UUID.randomUUID());
        em.flush();
        em.clear();

        var page = contacts.search(new ContactSearchCriteria(appA.getId(), null, null, null), 0, 10);

        assertEquals(3, page.getTotalElements());
        assertEquals(List.of("jane@a.com", "shop@a.com", "+1-555-0100"),
                page.getContent().stream().map(ContactEntity::getContent).toList());
    }

    @Test
    @DisplayName("the directory paginates on the server and applies type, status and text filters")
    void search_paginatesAndFilters() {
        for (int i = 0; i < 5; i++) {
            contact("user" + i + "@a.com", email, i != 4, appA, person, null, null);
        }
        contact("100%_sure@a.com", email, true, appA, null, "MERCHANT", merchantId);
        contact("+1-555-0100", phone, true, appA, null, "MERCHANT", merchantId);
        em.flush();
        em.clear();

        var first = contacts.search(new ContactSearchCriteria(appA.getId(), email.getId(), null, null), 0, 3);
        assertEquals(6, first.getTotalElements());
        assertEquals(2, first.getTotalPages());
        assertEquals(3, first.getContent().size());

        var second = contacts.search(new ContactSearchCriteria(appA.getId(), email.getId(), null, null), 1, 3);
        assertEquals(3, second.getContent().size());

        var inactive = contacts.search(new ContactSearchCriteria(appA.getId(), null, false, null), 0, 10);
        assertEquals(List.of("user4@a.com"), inactive.getContent().stream().map(ContactEntity::getContent).toList());

        var text = contacts.search(new ContactSearchCriteria(appA.getId(), null, null, "USER2"), 0, 10);
        assertEquals(List.of("user2@a.com"), text.getContent().stream().map(ContactEntity::getContent).toList());

        var wildcard = contacts.search(new ContactSearchCriteria(appA.getId(), null, null, "%"), 0, 10);
        assertEquals(List.of("100%_sure@a.com"),
                wildcard.getContent().stream().map(ContactEntity::getContent).toList());
    }

    @Test
    @DisplayName("person and external-owner lookups can be limited to one application")
    void lookups_byPersonAndOwner() {
        contact("jane@a.com", email, true, appA, person, null, null);
        contact("jane@b.com", email, true, appB, person, null, null);
        contact("shop@a.com", email, true, appA, null, "MERCHANT", merchantId);
        contact("shop@b.com", email, true, appB, null, "MERCHANT", merchantId);
        em.flush();
        em.clear();

        assertEquals(2, contacts.findByPersonIdWithGraph(person.getId(), null).size());
        assertEquals(List.of("jane@a.com"), contacts.findByPersonIdWithGraph(person.getId(), appA.getId()).stream()
                .map(ContactEntity::getContent).toList());
        assertEquals(2, contacts.findByOwnerWithGraph("MERCHANT", merchantId, null).size());
        assertEquals(List.of("shop@b.com"), contacts.findByOwnerWithGraph("MERCHANT", merchantId, appB.getId())
                .stream().map(ContactEntity::getContent).toList());
        assertTrue(contacts.findByOwnerWithGraph("OTHER", merchantId, null).isEmpty());
    }

    @Test
    @DisplayName("the feature catalog is paginated, searchable and never loads role links")
    void features_searchSummaries() {
        feature("USER_READ", "read users", true);
        feature("USER_WRITE", "write users", true);
        feature("REPORT_VIEW", "view 100%_reports", false);
        em.flush();
        em.clear();

        var all = features.searchSummaries(null, null, 0, 2);
        assertEquals(3, all.getTotalElements());
        assertEquals(List.of("REPORT_VIEW", "USER_READ"), all.getContent().stream().map(f -> f.name()).toList());

        var users = features.searchSummaries("user", true, 0, 10);
        assertEquals(List.of("USER_READ", "USER_WRITE"), users.getContent().stream().map(f -> f.name()).toList());

        var byDescription = features.searchSummaries("VIEW 100%", null, 0, 10);
        assertEquals(List.of("REPORT_VIEW"), byDescription.getContent().stream().map(f -> f.name()).toList());

        assertTrue(features.searchSummaries("%", null, 0, 10).getContent().stream()
                .allMatch(f -> f.description().contains("%")));
    }

    @Test
    @DisplayName("a feature lists the roles, and owning applications, it is linked to")
    void features_findRoleLinks() {
        FeatureEntity read = feature("USER_READ", "read users", true);
        RoleEntity admin = role("Admin", appA);
        RoleEntity viewer = role("Viewer", appB);
        link(admin, read);
        link(viewer, read);
        FeatureEntity unused = feature("UNUSED", "x", true);
        em.flush();
        em.clear();

        var links = features.findRoleLinks(read.getId(), 0, 10);

        assertEquals(2, links.getTotalElements());
        assertEquals(List.of("other-app", "shop-index"),
                links.getContent().stream().map(l -> l.applicationName()).sorted().toList());
        assertEquals(0, features.findRoleLinks(unused.getId(), 0, 10).getTotalElements());
    }

    private ApplicationEntity application(String name, String code) {
        ApplicationEntity application = new ApplicationEntity();
        application.setName(name);
        application.setCodeName(code);
        application.setDescription(name);
        application.setServiceTypeId(UUID.randomUUID());
        application.setActive(true);
        em.persist(application);
        return application;
    }

    private ContactTypeEntity contactType(String name) {
        ContactTypeEntity type = new ContactTypeEntity();
        type.setName(name);
        type.setActive(true);
        em.persist(type);
        return type;
    }

    private void contact(String content, ContactTypeEntity type, boolean active, ApplicationEntity application,
                         PersonEntity owner, String ownerType, UUID ownerId) {
        ContactEntity contact = new ContactEntity();
        contact.setContent(content);
        contact.setContactType(type);
        contact.setActive(active);
        contact.setApplication(application);
        contact.setPerson(owner);
        contact.setOwnerType(ownerType);
        contact.setOwnerId(ownerId);
        em.persist(contact);
    }

    private FeatureEntity feature(String name, String description, boolean active) {
        FeatureEntity feature = new FeatureEntity();
        feature.setName(name);
        feature.setDescription(description);
        feature.setActive(active);
        em.persist(feature);
        return feature;
    }

    private RoleEntity role(String name, ApplicationEntity application) {
        RoleEntity role = new RoleEntity();
        role.setName(name);
        role.setDescription(name);
        role.setActive(true);
        role.setApplication(application);
        em.persist(role);
        return role;
    }

    private void link(RoleEntity role, FeatureEntity feature) {
        RoleFeaturePK pk = new RoleFeaturePK();
        pk.setRoleId(role.getId());
        pk.setFeatureId(feature.getId());
        RoleFeatureEntity link = new RoleFeatureEntity();
        link.setRoleFeaturePK(pk);
        link.setRole(role);
        link.setFeature(feature);
        link.setActive(true);
        em.persist(link);
    }
}
