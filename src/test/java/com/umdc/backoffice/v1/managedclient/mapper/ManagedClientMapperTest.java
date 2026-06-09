package com.umdc.backoffice.v1.managedclient.mapper;

import com.umdc.backoffice.jpa.domain.ManagedClientEntity;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientCreateRequest;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientTO;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/// Unit tests for {@link ManagedClientMapper} verifying field mapping correctness
/// and AC-SEC-02 (no secret fields exposed in GET response DTO).
class ManagedClientMapperTest {

    // PMD AvoidDuplicateLiterals constants
    private static final String TEST_NAME  = "map-test-client";
    private static final String TEST_SCOPE = "read:data";
    private static final String SECRET_HASH = "$2a$10$mappedHash";

    private ManagedClientMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(ManagedClientMapper.class);
    }

    // ── toTO ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("toTO — maps all non-secret fields from entity to TO")
    void toTO_mapsAllFields() {
        UUID id = UUID.randomUUID();
        ManagedClientEntity entity = buildEntity(id);

        ManagedClientTO to = mapper.toTO(entity);

        assertNotNull(to);
        assertEquals(id, to.getClientId());
        assertEquals(TEST_NAME, to.getName());
        assertEquals(entity.getApplicationId(), to.getApplicationId());
        assertEquals(entity.getScopes(), to.getScopes());
        assertEquals(entity.isActive(), to.isActive());
    }

    @Test
    @DisplayName("toTO — ManagedClientTO does not expose secretHash field (AC-SEC-02)")
    void toTO_doesNotExposeSecretHash() {
        boolean hasSecretHash = Arrays.stream(ManagedClientTO.class.getDeclaredFields())
                .anyMatch(f -> "secretHash".equals(f.getName()) || "prevSecretHash".equals(f.getName()));
        assertFalse(hasSecretHash, "ManagedClientTO must not declare secretHash or prevSecretHash");
    }

    @Test
    @DisplayName("toTO — entity id maps to clientId on TO")
    void toTO_clientIdMappedFromEntityId() {
        UUID knownId = UUID.randomUUID();
        ManagedClientEntity entity = buildEntity(knownId);

        ManagedClientTO to = mapper.toTO(entity);

        assertEquals(knownId, to.getClientId());
    }

    // ── toEntity ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("toEntity — id and secretHash are null on mapped entity")
    void toEntity_ignoresIdAndHashes() {
        ManagedClientCreateRequest request = buildCreateRequest();

        ManagedClientEntity entity = mapper.toEntity(request);

        assertNotNull(entity);
        assertNull(entity.getId());
        assertNull(entity.getSecretHash());
        assertNull(entity.getPrevSecretHash());
    }

    // ── updateEntityFromRequest ───────────────────────────────────────────────

    @Test
    @DisplayName("updateEntityFromRequest — null fields in request leave entity values unchanged")
    void updateEntityFromRequest_nullFieldsPreserved() {
        UUID id = UUID.randomUUID();
        ManagedClientEntity entity = buildEntity(id);
        String originalName = entity.getName();
        ManagedClientUpdateRequest request = new ManagedClientUpdateRequest();
        // name is null — must not overwrite entity

        mapper.updateEntityFromRequest(request, entity);

        assertEquals(originalName, entity.getName());
    }

    @Test
    @DisplayName("updateEntityFromRequest — non-null name in request updates entity name")
    void updateEntityFromRequest_nonNullNameUpdatesEntity() {
        UUID id = UUID.randomUUID();
        ManagedClientEntity entity = buildEntity(id);
        ManagedClientUpdateRequest request = new ManagedClientUpdateRequest();
        request.setName("new-name");

        mapper.updateEntityFromRequest(request, entity);

        assertEquals("new-name", entity.getName());
    }

    // ── toTOList ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("toTOList — maps all elements without secret fields")
    void toTOList_mapsAllElements() {
        List<ManagedClientEntity> entities = List.of(
                buildEntity(UUID.randomUUID()),
                buildEntity(UUID.randomUUID()),
                buildEntity(UUID.randomUUID()));

        List<ManagedClientTO> tos = mapper.toTOList(entities);

        assertEquals(3, tos.size());
        tos.forEach(to -> {
            assertNotNull(to.getClientId());
            assertNotNull(to.getName());
        });
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private ManagedClientEntity buildEntity(UUID id) {
        ManagedClientEntity entity = new ManagedClientEntity();
        entity.setId(id);
        entity.setName(TEST_NAME);
        entity.setApplicationId(UUID.randomUUID());
        entity.setScopes(List.of(TEST_SCOPE));
        entity.setActive(true);
        entity.setSecretHash(SECRET_HASH);
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }

    private ManagedClientCreateRequest buildCreateRequest() {
        ManagedClientCreateRequest req = new ManagedClientCreateRequest();
        req.setName(TEST_NAME);
        req.setApplicationId(UUID.randomUUID());
        req.setScopes(List.of(TEST_SCOPE));
        req.setActive(true);
        return req;
    }
}
