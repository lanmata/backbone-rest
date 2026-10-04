/*
 *  @(#)ContactCreateRequest.java
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

package com.umdc.backoffice.v1.contacts.api.to;

import java.util.UUID;

/**
 * Wire shape for {@code POST /api/v1/contacts/}: a flat body referencing the contact type by
 * id ({@code contentTypeId}) instead of embedding a full {@code ContactType} object — the
 * {@code com.umdc.commons.general.pojo.Contact} DTO itself has no such field, so this request
 * is translated into a {@code Contact} (with its {@code contactType} populated from a lookup)
 * before reaching {@link com.umdc.backoffice.v1.contacts.service.ContactService}.
 *
 * @param id             optional client-supplied contact id
 * @param content        the contact value (phone number, email, etc.)
 * @param contentTypeId  the id of the existing {@code ContactType} this contact belongs to
 * @param personId       the id of the person this contact belongs to
 * @param applicationId  the id of the owning application
 * @param active         whether the contact is active
 */
public record ContactCreateRequest(
        UUID id,
        String content,
        UUID contentTypeId,
        UUID personId,
        UUID applicationId,
        Boolean active
) {
}
