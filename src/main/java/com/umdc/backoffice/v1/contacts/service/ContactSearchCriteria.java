package com.umdc.backoffice.v1.contacts.service;

import java.util.UUID;

/**
 * Filters for the application-scoped contact directory. {@code applicationId} is mandatory: contacts
 * are never listed across applications.
 *
 * @param applicationId the owning application (required)
 * @param contactTypeId optional contact type filter
 * @param active        optional active/inactive filter
 * @param q             optional case-insensitive "contains" filter on the contact content
 */
public record ContactSearchCriteria(UUID applicationId, UUID contactTypeId, Boolean active, String q) {
}
