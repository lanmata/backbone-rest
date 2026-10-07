/*
 *  @(#)UserGraphLookupServiceImplIntegrationTest.java
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

package com.umdc.backoffice.v1.users.service;

import com.umdc.backoffice.v1.roles.service.RoleGraphLookupServiceImpl;
import com.umdc.persistence.general.domains.ApplicationEntity;
import com.umdc.persistence.general.domains.ApplicationRoleUserEntity;
import com.umdc.persistence.general.domains.ApplicationRoleUserEntityId;
import com.umdc.persistence.general.domains.ContactEntity;
import com.umdc.persistence.general.domains.ContactTypeEntity;
import com.umdc.persistence.general.domains.PersonEntity;
import com.umdc.persistence.general.domains.RoleEntity;
import com.umdc.persistence.general.domains.UserEntity;
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
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.umdc.backoffice.constant.BackboneAppConstants.ENTITY_PACKAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test proving the {@link UserGraphLookupServiceImpl} fix for the bag/set
 * cartesian-product bug: a real Hibernate session (H2, schema auto-generated from the actual
 * {@code com.umdc.persistence} entity mappings) is used deliberately instead of a mocked
 * {@link jakarta.persistence.EntityManager} — the unit test suite mocks {@code EntityManager}
 * entirely, so it can never observe row-multiplication produced by Hibernate's own JOIN FETCH
 * result mapping.
 * <p>
 * Bootstrapped from the minimal {@link MinimalJpaTestConfig} below rather than the real
 * {@code UMDCBackofficeRestApplication} — {@code @DataJpaTest} falls back to the nearest
 * {@code @SpringBootConfiguration} class, and the real one pulls in every {@code @Bean} from
 * {@code DataSourceSslConfig} etc., none satisfiable without live Vault/SSL infrastructure. Same
 * root cause as {@code PrxBackofficeRestApplicationTest}.
 * <p>
 * {@code application.yml} itself is still loaded regardless — it's a classpath resource Spring
 * Boot's environment post-processing picks up unconditionally, independent of which
 * {@code @SpringBootConfiguration} class roots the context — so {@code SPRING_BOOT_PROFILE_ACTIVE}
 * (undefaulted there, normally supplied by the environment) is overridden here to a harmless
 * value; {@code spring.config.import}'s Vault/Config-Server entries are both {@code optional:},
 * so they don't block a context with neither reachable. {@code DataJpaRepositoriesAutoConfiguration}
 * is excluded — repository base-package scanning requires an {@code @EnableAutoConfiguration}
 * root (which {@link MinimalJpaTestConfig} deliberately doesn't have) and isn't needed here: this
 * test drives {@link UserGraphLookupServiceImpl} directly via {@link TestEntityManager}, no
 * Spring Data repository interface involved.
 */
@DataJpaTest(excludeAutoConfiguration = DataJpaRepositoriesAutoConfiguration.class)
@ContextConfiguration(classes = UserGraphLookupServiceImplIntegrationTest.MinimalJpaTestConfig.class)
@TestPropertySource(properties = {
        "SPRING_BOOT_PROFILE_ACTIVE=test",
        // NON_KEYWORDS=USER: "user" is an H2 reserved word but a real (unquoted) table name here.
        "spring.datasource.url=jdbc:h2:mem:userGraphLookupTest;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:user-graph-lookup-test-schema.sql"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan(basePackages = ENTITY_PACKAGE)
class UserGraphLookupServiceImplIntegrationTest {

    @SpringBootConfiguration
    static class MinimalJpaTestConfig {
    }

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    @DisplayName("a user with 2 contacts and 3 application-role links comes back with exactly "
            + "2 contacts and 3 links, not a 2x3=6 cartesian product")
    void findByIdWithGraph_doesNotCartesianProductContactsAndApplicationRoleUser() {
        // Three applications: application_role_user has a UNIQUE(user_id, application_id)
        // constraint, so three distinct role links for one user require three applications.
        ApplicationEntity app1 = persistApplication("app-one", "APP1");
        ApplicationEntity app2 = persistApplication("app-two", "APP2");
        ApplicationEntity app3 = persistApplication("app-three", "APP3");

        RoleEntity role1 = persistRole("ROLE_ONE", app1);
        RoleEntity role2 = persistRole("ROLE_TWO", app2);
        RoleEntity role3 = persistRole("ROLE_THREE", app3);

        ContactTypeEntity contactType = persistContactType("EMAIL");

        PersonEntity person = new PersonEntity();
        person.setName("Jane");
        person.setLastName("Doe");
        person.setGender("F");
        person.setBirthdate(LocalDate.of(1990, 1, 1));
        testEntityManager.persist(person);

        // UserEntity.id is @GeneratedValue — must stay null pre-persist, see persistRole() above.
        UserEntity user = new UserEntity();
        user.setAlias("jane.doe");
        user.setPassword(UUID.randomUUID().toString());
        user.setEmail("jane.doe@example.com");
        user.setDisplayName("Jane Doe");
        user.setActive(true);
        user.setCreatedDate(LocalDateTime.now());
        user.setLastUpdate(LocalDateTime.now());
        user.setNotificationEmail(true);
        user.setNotificationSms(false);
        user.setPrivacyDataOutActive(false);
        user.setApplication(app1);
        user.setPerson(person);
        testEntityManager.persist(user);

        persistContact("jane.doe@personal.example", contactType, person);
        persistContact("+1-555-0100", contactType, person);

        persistApplicationRoleUser(user, app1, role1);
        persistApplicationRoleUser(user, app2, role2);
        persistApplicationRoleUser(user, app3, role3);

        testEntityManager.flush();
        testEntityManager.clear(); // force a real SELECT — no stale in-context objects

        var roleGraphLookupService = new RoleGraphLookupServiceImpl(testEntityManager.getEntityManager());
        var service = new UserGraphLookupServiceImpl(testEntityManager.getEntityManager(), roleGraphLookupService);
        var result = service.findByIdWithGraph(user.getId());

        assertTrue(result.isPresent());
        UserEntity loaded = result.get();
        assertEquals(2, loaded.getPerson().getContacts().size(),
                "contacts must not be duplicated by the applicationRoleUser join");
        assertEquals(3, loaded.getApplicationRoleUser().size(),
                "applicationRoleUser must not be duplicated by the contacts join");

        Set<String> contactContents = loaded.getPerson().getContacts().stream()
                .map(ContactEntity::getContent)
                .collect(Collectors.toSet());
        assertEquals(Set.of("jane.doe@personal.example", "+1-555-0100"), contactContents);
    }

    private ApplicationEntity persistApplication(String name, String codeName) {
        ApplicationEntity application = new ApplicationEntity();
        application.setName(name);
        application.setCodeName(codeName);
        application.setDescription(name);
        application.setServiceTypeId(UUID.randomUUID());
        application.setActive(true);
        testEntityManager.persist(application);
        return application;
    }

    private RoleEntity persistRole(String name, ApplicationEntity application) {
        // RoleEntity.id is @GeneratedValue — must stay null pre-persist, same as the
        // IDENTITY-strategy entities above (Hibernate treats any pre-set id as "detached").
        RoleEntity role = new RoleEntity();
        role.setName(name);
        role.setDescription(name);
        role.setActive(true);
        role.setApplication(application);
        testEntityManager.persist(role);
        return role;
    }

    private ContactTypeEntity persistContactType(String name) {
        // ContactTypeEntity.id is @GeneratedValue(IDENTITY) — see persistApplication() above.
        ContactTypeEntity contactType = new ContactTypeEntity();
        contactType.setName(name);
        contactType.setActive(true);
        testEntityManager.persist(contactType);
        return contactType;
    }

    private void persistContact(String content, ContactTypeEntity contactType, PersonEntity person) {
        // ContactEntity.id is @GeneratedValue(IDENTITY) — see persistApplication() above.
        ContactEntity contact = new ContactEntity();
        contact.setContent(content);
        contact.setContactType(contactType);
        contact.setActive(true);
        contact.setPerson(person);
        testEntityManager.persist(contact);
    }

    private void persistApplicationRoleUser(UserEntity user, ApplicationEntity application, RoleEntity role) {
        var id = new ApplicationRoleUserEntityId();
        id.setUserId(user.getId());
        id.setRoleId(role.getId());
        id.setApplicationId(application.getId());

        var aru = new ApplicationRoleUserEntity();
        aru.setId(id);
        aru.setUser(user);
        aru.setRole(role);
        aru.setApplication(application);
        aru.setActive(true);
        testEntityManager.persist(aru);
    }
}
