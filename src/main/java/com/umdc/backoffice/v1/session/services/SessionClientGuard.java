/*
 *  @(#)SessionClientGuard.java
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
package com.umdc.backoffice.v1.session.services;

import com.umdc.backoffice.property.SessionClientProperties;
import com.umdc.persistence.general.domains.ManagedClientEntity;
import com.umdc.persistence.general.repositories.ManagedClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Binds a user session to the application client that asks for it.
 *
 * <p>An application front end (its BFF) authenticates itself with a managed client (MCAM, client credentials) and sends
 * that M2M token as {@code Authorization: Bearer} when it opens, renews or refreshes a user session. Then:
 * <ul>
 *   <li>the client must be active and belong to the application of the session, otherwise {@code 403};</li>
 *   <li>an application listed in {@link SessionClientProperties} cannot open sessions without a client: {@code 401}.</li>
 * </ul>
 * {@link com.umdc.backoffice.security.filter.ManagedClientTokenFilter} has already verified the token (signature,
 * expiry, revocation) and left the client id in the request.
 */
@Component
public class SessionClientGuard {

    /** Request attribute holding the id of the managed client authenticated for this request. */
    public static final String CLIENT_ID_ATTRIBUTE = "umdc.mcam.clientId";

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionClientGuard.class);

    private final ManagedClientRepository repository;
    private final SessionClientProperties properties;

    public SessionClientGuard(ManagedClientRepository repository, SessionClientProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    /**
     * @param applicationId application of the session ({@code null} for a legacy token that does not name one)
     * @return empty when the request may go on; otherwise the status to answer with
     */
    public Optional<HttpStatus> reject(UUID applicationId) {
        Optional<UUID> clientId = currentClientId();
        if (clientId.isEmpty()) {
            if (properties.isRequiredFor(applicationId)) {
                LOGGER.warn("Session request for application '{}' without a managed client", applicationId);
                return Optional.of(HttpStatus.UNAUTHORIZED);
            }
            return Optional.empty();
        }
        Optional<ManagedClientEntity> client = repository.findById(clientId.get());
        if (client.isEmpty() || !client.get().isActive()
                || !Objects.equals(client.get().getApplicationId(), applicationId)) {
            LOGGER.warn("Managed client '{}' may not handle sessions of application '{}'", clientId.get(), applicationId);
            return Optional.of(HttpStatus.FORBIDDEN);
        }
        return Optional.empty();
    }

    private static Optional<UUID> currentClientId() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (Objects.isNull(attributes)) {
            return Optional.empty();
        }
        Object value = attributes.getAttribute(CLIENT_ID_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        if (Objects.isNull(value)) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(value.toString()));
        } catch (IllegalArgumentException _) {
            // an authenticated client whose id cannot be resolved is not a client we can match: refuse it
            return Optional.of(new UUID(0L, 0L));
        }
    }
}
