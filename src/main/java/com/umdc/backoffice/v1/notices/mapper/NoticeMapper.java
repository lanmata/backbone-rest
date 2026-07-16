/*
 *  @(#)NoticeMapper.java
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
package com.umdc.backoffice.v1.notices.mapper;

import com.umdc.backoffice.v1.notices.api.to.Notice;
import com.umdc.commons.services.config.mapper.MapperAppConfig;
import com.umdc.persistence.general.domains.NoticeEntity;
import com.umdc.persistence.general.domains.NoticeId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper interface for converting between {@link Notice} and {@link NoticeEntity}.
 * Utilizes MapStruct for automatic mapping.
 * <p>
 * {@link NoticeEntity} has no surrogate id: its identity is the composite
 * {@link NoticeId} embedded key, while {@link Notice} is a flat DTO exposing
 * {@code userId}, {@code applicationId} and {@code noticeTypeId} directly.
 * The {@code userId}/{@code applicationId}/{@code noticeTypeId} properties are
 * therefore derived explicitly on the read direction, and the composite id is
 * (re)built explicitly on the write direction via {@link #toNoticeId(Notice)}.
 * The {@code noticeType} relation is intentionally left unmapped; the
 * {@code NoticeServiceImpl} is responsible for resolving and setting the
 * {@link com.umdc.persistence.general.domains.NoticeTypeEntity} relation before
 * persisting.
 * </p>
 */
@Mapper(
        config = MapperAppConfig.class
)
@MapperConfig(
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface NoticeMapper {

    /**
     * Converts a {@link NoticeEntity} to a {@link Notice}.
     *
     * @param entity the entity to convert
     * @return the converted DTO
     */
    @Mapping(target = "userId", expression = "java(entity.getId() != null ? entity.getId().getUserId() : null)")
    @Mapping(target = "applicationId", expression = "java(entity.getId() != null ? entity.getId().getApplicationId() : null)")
    @Mapping(target = "noticeTypeId", expression = "java(entity.getId() != null ? entity.getId().getNoticeTypeId() : null)")
    Notice toTarget(NoticeEntity entity);

    /**
     * Converts a {@link Notice} to a {@link NoticeEntity}.
     * The {@code noticeType} relation is left unset; the caller must resolve and assign it.
     *
     * @param notice the DTO to convert
     * @return the converted entity
     */
    @Mapping(target = "id", expression = "java(toNoticeId(notice))")
    @Mapping(target = "noticeType", ignore = true)
    NoticeEntity toSource(Notice notice);

    /**
     * Builds the composite {@link NoticeId} from the flat identifier fields of a {@link Notice}.
     *
     * @param notice the DTO carrying the identifier fields
     * @return the composite id, or {@code null} if the notice is {@code null}
     */
    default NoticeId toNoticeId(Notice notice) {
        if (notice == null) {
            return null;
        }
        NoticeId id = new NoticeId();
        id.setUserId(notice.getUserId());
        id.setApplicationId(notice.getApplicationId());
        id.setNoticeTypeId(notice.getNoticeTypeId());
        return id;
    }
}
