package com.umdc.backoffice.v1.users.api.to;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * PutUserUpdateRequest.
 * <p>
 * Record representing a request to update a user with all necessary fields.
 * All fields except application, active, notificationEmail, notificationSms, and privacyDataOutActive are optional.
 * Only non-null fields will be updated, allowing partial updates.
 * </p>
 */
public record PutUserUpdateRequest(
        @NotNull
        UUID application,
        String password,
        String displayName,
        Boolean active,
        Boolean notificationEmail,
        Boolean notificationSms,
        Boolean privacyDataOutActive,
        String firstName,
        String middleName,
        String lastName,
        String gender,
        LocalDate birthdate,
        List<Contact> contacts,
        List<UUID> roleIds
) {
    /**
     * Contact.
     * <p>
     * Record representing a user's contact information.
     * </p>
     */
    public record Contact(
            UUID id,
            String content,
            ContactType contactType,
            boolean active
    ) {
        @Override
        public String toString() {
            return "Contact{" +
                    "id=" + id +
                    ", content='" + content + '\'' +
                    ", contactType=" + contactType +
                    ", active=" + active +
                    '}';
        }
    }

    /**
     * ContactType.
     * <p>
     * Record representing the type of contact.
     * </p>
     */
    public record ContactType(
            UUID id
    ) {
        @Override
        public String toString() {
            return "ContactType{" +
                    "id=" + id +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "PutUserUpdateRequest{" +
                "password='" + password + '\'' +
                ", displayName='" + displayName + '\'' +
                ", active=" + active +
                ", notificationEmail=" + notificationEmail +
                ", notificationSms=" + notificationSms +
                ", privacyDataOutActive=" + privacyDataOutActive +
                ", firstName='" + firstName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", gender='" + gender + '\'' +
                ", birthdate=" + birthdate +
                ", contacts=" + contacts +
                ", roleIds=" + roleIds +
                '}';
    }
}
