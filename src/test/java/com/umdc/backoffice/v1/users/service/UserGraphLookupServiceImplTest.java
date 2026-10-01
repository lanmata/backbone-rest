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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/// Unit tests for [UserGraphLookupServiceImpl]. These exercise the two-query dispatch logic only
/// (which JPQL goes out, in what order, short-circuiting when the user isn't found) — proving the
/// two queries are never merged back into one (the original bag/set cartesian-product bug) is the
/// job of [UserGraphLookupServiceImplIntegrationTest], which runs against a real Hibernate
/// session; a mocked [EntityManager] can't reproduce row-multiplication from a real JOIN FETCH.
class UserGraphLookupServiceImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<UserEntity> applicationAndContactsQuery;

    @Mock
    private TypedQuery<UserEntity> applicationRoleUserQuery;

    private UserGraphLookupServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new UserGraphLookupServiceImpl(entityManager);

        when(entityManager.createQuery(
                argThat(jpql -> jpql != null && !jpql.contains("applicationRoleUser")), eq(UserEntity.class)))
                .thenReturn(applicationAndContactsQuery);
        when(entityManager.createQuery(
                argThat(jpql -> jpql != null && jpql.contains("applicationRoleUser")), eq(UserEntity.class)))
                .thenReturn(applicationRoleUserQuery);
        when(applicationAndContactsQuery.setParameter(eq("userId"), any())).thenReturn(applicationAndContactsQuery);
        when(applicationRoleUserQuery.setParameter(eq("userId"), any())).thenReturn(applicationRoleUserQuery);
    }

    @Test
    @DisplayName("runs a second query to populate applicationRoleUser on the same managed instance the first query returned")
    void findByIdWithGraph_runsSecondQuery_whenFirstFindsUser() {
        UUID userId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        when(applicationAndContactsQuery.getResultStream()).thenReturn(Stream.of(userEntity));
        when(applicationRoleUserQuery.getResultStream()).thenReturn(Stream.of(userEntity));

        Optional<UserEntity> result = service.findByIdWithGraph(userId);

        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
        verify(applicationAndContactsQuery).setParameter("userId", userId);
        verify(applicationRoleUserQuery).setParameter("userId", userId);
        verify(entityManager, times(2)).createQuery(any(String.class), eq(UserEntity.class));
    }

    @Test
    @DisplayName("short-circuits and never runs the second query when the user isn't found")
    void findByIdWithGraph_skipsSecondQuery_whenUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(applicationAndContactsQuery.getResultStream()).thenReturn(Stream.empty());

        Optional<UserEntity> result = service.findByIdWithGraph(userId);

        assertFalse(result.isPresent());
        verify(entityManager, times(1)).createQuery(any(String.class), eq(UserEntity.class));
        verify(applicationRoleUserQuery, never()).getResultStream();
    }

    @Test
    @DisplayName("the two queries each JOIN FETCH only one to-many collection — never both together")
    void findByIdWithGraph_neverJoinsBothToManyCollectionsInOneQuery() {
        UUID userId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        when(applicationAndContactsQuery.getResultStream()).thenReturn(Stream.of(userEntity));
        when(applicationRoleUserQuery.getResultStream()).thenReturn(Stream.of(userEntity));

        service.findByIdWithGraph(userId);

        // First query: application + person + person.contacts (a bag) — safe alone.
        verify(entityManager).createQuery(argThat(jpql ->
                jpql.contains("LEFT JOIN FETCH u.application")
                        && jpql.contains("LEFT JOIN FETCH u.person p")
                        && jpql.contains("LEFT JOIN FETCH p.contacts")
                        && !jpql.contains("applicationRoleUser")
                        && jpql.contains("WHERE u.id = :userId")), eq(UserEntity.class));
        // Second query: applicationRoleUser (a Set) + its nested role/application — safe alone,
        // and critically does NOT also join the contacts bag (that combination is the bug).
        verify(entityManager).createQuery(argThat(jpql ->
                jpql.contains("LEFT JOIN FETCH u.applicationRoleUser ar")
                        && jpql.contains("LEFT JOIN FETCH ar.role")
                        && jpql.contains("LEFT JOIN FETCH ar.application")
                        && !jpql.contains("contacts")
                        && jpql.contains("WHERE u.id = :userId")), eq(UserEntity.class));
    }
}
