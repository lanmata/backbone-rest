package com.umdc.backoffice.v1.features.api.to;

import java.util.UUID;

/**
 * A role a feature is linked to, with the application that owns the role.
 *
 * @param roleId          role id
 * @param roleName        role name
 * @param roleActive      whether the role is active
 * @param applicationId   owning application id
 * @param applicationName owning application name
 */
public record FeatureRoleLink(UUID roleId, String roleName, boolean roleActive, UUID applicationId,
                              String applicationName) {
}
