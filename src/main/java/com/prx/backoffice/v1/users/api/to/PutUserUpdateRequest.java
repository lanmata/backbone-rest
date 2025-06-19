package com.prx.backoffice.v1.users.api.to;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * PutUserUpdateRequest.
 * <p>
 * Record representing a request to update a user with all necessary fields.
 * </p>
 */
public record PutUserUpdateRequest(
        String password,
        String displayName,
        boolean active,
        boolean notificationEmail,
        boolean notificationSms,
        boolean privacyDataOutActive,
        String firstName,
        String middleName,
        String lastName,
        String gender,
        LocalDate birthdate,
        List<Contact> contacts
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
                '}';
    }
}
