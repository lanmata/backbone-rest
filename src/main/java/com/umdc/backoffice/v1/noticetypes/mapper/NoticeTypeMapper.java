/*
 *  @(#)NoticeTypeMapper.java
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
package com.umdc.backoffice.v1.noticetypes.mapper;

import com.umdc.backoffice.v1.noticetypes.api.to.NoticeType;
import com.umdc.commons.services.config.mapper.MapperAppConfig;
import com.umdc.persistence.general.domains.NoticeTypeEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper interface for converting between {@link NoticeType} and {@link NoticeTypeEntity}.
 * Utilizes MapStruct for automatic mapping.
 */
@Mapper(
        config = MapperAppConfig.class
)
@MapperConfig(
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface NoticeTypeMapper {

    /**
     * Converts a {@link NoticeTypeEntity} to a {@link NoticeType}.
     *
     * @param entity the entity to convert
     * @return the converted POJO
     */
    NoticeType toTarget(NoticeTypeEntity entity);

    /**
     * Converts a {@link NoticeType} to a {@link NoticeTypeEntity}.
     *
     * @param noticeType the POJO to convert
     * @return the converted entity
     */
    @InheritInverseConfiguration
    NoticeTypeEntity toSource(NoticeType noticeType);
}
