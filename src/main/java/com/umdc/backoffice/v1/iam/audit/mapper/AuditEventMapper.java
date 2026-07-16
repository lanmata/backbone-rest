/*
 *  @(#)AuditEventMapper.java
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
import com.umdc.commons.services.config.mapper.MapperAppConfig;
import com.umdc.persistence.general.domains.AuditEventEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper that converts {@link AuditEventEntity} instances to
 * {@link AuditEventTO} transfer objects.
 * <p>
 * All fields share the same names so no explicit {@code @Mapping} annotations
 * are required. The mapper is Spring-managed via {@link MapperAppConfig}.
 * </p>
 */
@Mapper(config = MapperAppConfig.class)
public interface AuditEventMapper {

    /**
     * Converts a single {@link AuditEventEntity} to an {@link AuditEventTO}.
     *
     * @param entity the source entity
     * @return the mapped transfer object
     */
    AuditEventTO toTO(AuditEventEntity entity);

    /**
     * Converts a list of {@link AuditEventEntity} to a list of {@link AuditEventTO}.
     *
     * @param entities the source entity list
     * @return the mapped transfer object list
     */
    List<AuditEventTO> toTOList(List<AuditEventEntity> entities);
}

