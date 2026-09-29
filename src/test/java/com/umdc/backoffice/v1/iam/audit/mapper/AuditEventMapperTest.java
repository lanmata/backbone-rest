/*
 *  @(#)AuditEventMapperTest.java
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
package com.umdc.backoffice.v1.iam.audit.mapper;

import com.umdc.backoffice.v1.iam.audit.api.to.AuditEventTO;
import com.umdc.commons.general.pojo.AuditEventType;
import com.umdc.persistence.general.domains.AuditEventEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Luis Mata
 */
class AuditEventMapperTest {

    private final AuditEventMapper mapper = Mappers.getMapper(AuditEventMapper.class);

    @Test
    @DisplayName("toTO copies every field by name")
    void toTOCopiesEveryField() {
        AuditEventEntity entity = new AuditEventEntity();
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.now().minusMinutes(5);
        LocalDateTime createdAt = LocalDateTime.now();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setApplicationId(applicationId);
        entity.setEventType(AuditEventType.LOGIN_SUCCESS);
        entity.setIpAddress("127.0.0.1");
        entity.setUserAgent("JUnit");
        entity.setOccurredAt(occurredAt);
        entity.setDetails("{\"action\":\"login\"}");
        entity.setCreatedAt(createdAt);

        var to = mapper.toTO(entity);

        assertEquals(id, to.id());
        assertEquals(userId, to.userId());
        assertEquals(applicationId, to.applicationId());
        assertEquals(AuditEventType.LOGIN_SUCCESS, to.eventType());
        assertEquals("127.0.0.1", to.ipAddress());
        assertEquals("JUnit", to.userAgent());
        assertEquals(occurredAt, to.occurredAt());
        assertEquals("{\"action\":\"login\"}", to.details());
        assertEquals(createdAt, to.createdAt());
    }

    @Test
    @DisplayName("toTO returns null for a null entity")
    void toTOReturnsNullForNullEntity() {
        assertNull(mapper.toTO(null));
    }

    @Test
    @DisplayName("toTOList maps every entity in the list, preserving order")
    void toTOListMapsEveryEntity() {
        AuditEventEntity first = new AuditEventEntity();
        first.setId(UUID.randomUUID());
        first.setEventType(AuditEventType.LOGIN_SUCCESS);
        AuditEventEntity second = new AuditEventEntity();
        second.setId(UUID.randomUUID());
        second.setEventType(AuditEventType.LOGOUT);

        List<AuditEventTO> result = mapper.toTOList(List.of(first, second));

        assertEquals(2, result.size());
        assertEquals(first.getId(), result.get(0).id());
        assertEquals(AuditEventType.LOGIN_SUCCESS, result.get(0).eventType());
        assertEquals(second.getId(), result.get(1).id());
        assertEquals(AuditEventType.LOGOUT, result.get(1).eventType());
    }

    @Test
    @DisplayName("toTOList returns null for a null list")
    void toTOListReturnsNullForNullList() {
        assertNull(mapper.toTOList(null));
    }
}
