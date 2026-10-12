package com.umdc.backoffice.v1.session.services;

import com.umdc.backoffice.property.SessionClientProperties;
import com.umdc.persistence.general.domains.ManagedClientEntity;
import com.umdc.persistence.general.repositories.ManagedClientRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SessionClientGuardTest {

    private final UUID appId = UUID.randomUUID();
    private final UUID clientId = UUID.randomUUID();
    private final ManagedClientRepository repository = mock(ManagedClientRepository.class);
    private final SessionClientProperties properties = new SessionClientProperties();
    private final SessionClientGuard guard = new SessionClientGuard(repository, properties);
    private MockHttpServletRequest request;

    @BeforeEach
    void bindRequest() {
        request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void unbindRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    private void authenticatedClient(UUID application, boolean active) {
        request.setAttribute(SessionClientGuard.CLIENT_ID_ATTRIBUTE, clientId.toString());
        ManagedClientEntity client = mock(ManagedClientEntity.class);
        when(client.getApplicationId()).thenReturn(application);
        when(client.isActive()).thenReturn(active);
        when(repository.findById(clientId)).thenReturn(Optional.of(client));
    }

    @Test
    void withoutAClientAnApplicationThatDoesNotNeedOneGoesOn() {
        assertEquals(Optional.empty(), guard.reject(appId));
    }

    @Test
    void withoutAClientAnApplicationThatNeedsOneIsUnauthorized() {
        properties.setRequiredApplications(List.of(appId));

        assertEquals(Optional.of(HttpStatus.UNAUTHORIZED), guard.reject(appId));
        assertEquals(Optional.empty(), guard.reject(UUID.randomUUID()));
    }

    @Test
    void whenEveryApplicationNeedsAClientEvenALegacyTokenWithoutApplicationIsUnauthorized() {
        properties.setRequired(true);

        assertEquals(Optional.of(HttpStatus.UNAUTHORIZED), guard.reject(appId));
        assertEquals(Optional.of(HttpStatus.UNAUTHORIZED), guard.reject(null));
    }

    @Test
    void theClientOfTheApplicationGoesOn() {
        properties.setRequiredApplications(List.of(appId));
        authenticatedClient(appId, true);

        assertEquals(Optional.empty(), guard.reject(appId));
    }

    @Test
    void aClientOfAnotherApplicationIsForbiddenEvenWhenNoClientIsRequired() {
        authenticatedClient(UUID.randomUUID(), true);

        assertEquals(Optional.of(HttpStatus.FORBIDDEN), guard.reject(appId));
    }

    @Test
    void aClientCannotHandleASessionThatNamesNoApplication() {
        authenticatedClient(appId, true);

        assertEquals(Optional.of(HttpStatus.FORBIDDEN), guard.reject(null));
    }

    @Test
    void anInactiveOrUnknownClientIsForbidden() {
        authenticatedClient(appId, false);
        assertEquals(Optional.of(HttpStatus.FORBIDDEN), guard.reject(appId));

        when(repository.findById(clientId)).thenReturn(Optional.empty());
        assertEquals(Optional.of(HttpStatus.FORBIDDEN), guard.reject(appId));
    }

    @Test
    void aClientIdThatIsNotAUuidIsForbidden() {
        request.setAttribute(SessionClientGuard.CLIENT_ID_ATTRIBUTE, "not-a-uuid");

        assertEquals(Optional.of(HttpStatus.FORBIDDEN), guard.reject(appId));
    }

    @Test
    void outsideARequestThereIsNoClient() {
        RequestContextHolder.resetRequestAttributes();
        properties.setRequiredApplications(null);   // tolerated: nothing is required

        assertEquals(Optional.empty(), guard.reject(appId));
        assertEquals(List.of(), properties.getRequiredApplications());
    }
}
