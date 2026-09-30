/*
 *  @(#)UserGraphLookupServiceImplTest.java
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

import com.umdc.persistence.general.domains.UserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/// Unit tests for [UserGraphLookupServiceImpl].
class UserGraphLookupServiceImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<UserEntity> typedQuery;

    private UserGraphLookupServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new UserGraphLookupServiceImpl(entityManager);
        when(entityManager.createQuery(anyString(), eq(UserEntity.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("userId"), any())).thenReturn(typedQuery);
    }

    @Test
    @DisplayName("returns the user when the JOIN FETCH query finds one")
    void findByIdWithGraph_returnsUser_whenFound() {
        UUID userId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        when(typedQuery.getResultStream()).thenReturn(Stream.of(userEntity));

        Optional<UserEntity> result = service.findByIdWithGraph(userId);

        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
        verify(typedQuery).setParameter("userId", userId);
    }

    @Test
    @DisplayName("returns empty when no user matches the id")
    void findByIdWithGraph_returnsEmpty_whenNotFound() {
        UUID userId = UUID.randomUUID();
        when(typedQuery.getResultStream()).thenReturn(Stream.empty());

        Optional<UserEntity> result = service.findByIdWithGraph(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("JPQL eagerly JOIN FETCHes every lazy association that crashes native-image")
    void findByIdWithGraph_jpqlFetchesFullLazyGraph() {
        UUID userId = UUID.randomUUID();
        when(typedQuery.getResultStream()).thenReturn(Stream.empty());

        service.findByIdWithGraph(userId);

        ArgumentCaptor<String> jpqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createQuery(jpqlCaptor.capture(), eq(UserEntity.class));
        String jpql = jpqlCaptor.getValue();

        assertTrue(jpql.contains("LEFT JOIN FETCH u.application"), "must fetch the top-level lazy application");
        assertTrue(jpql.contains("LEFT JOIN FETCH u.person p"), "must fetch person (LEFT, not INNER — it's optional)");
        assertTrue(jpql.contains("LEFT JOIN FETCH p.contacts"), "must fetch person.contacts");
        assertTrue(jpql.contains("LEFT JOIN FETCH u.applicationRoleUser ar"), "must fetch applicationRoleUser");
        assertTrue(jpql.contains("LEFT JOIN FETCH ar.role"), "must fetch the nested lazy role");
        assertTrue(jpql.contains("LEFT JOIN FETCH ar.application"), "must fetch the nested lazy application");
        assertTrue(jpql.contains("WHERE u.id = :userId"), "must filter by the requested id");
    }
}
