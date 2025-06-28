package com.prx.backoffice.v1.users.api.to;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PutUserUpdateRequest record.
 */
class PutUserUpdateRequestTest {

    @Test
    @DisplayName("Should create record and validate all getters")
    void testRecordCreationAndGetters() {
        UUID contactId = UUID.randomUUID();
        UUID contactTypeId = UUID.randomUUID();
        PutUserUpdateRequest.ContactType contactType = new PutUserUpdateRequest.ContactType(contactTypeId);
        PutUserUpdateRequest.Contact contact = new PutUserUpdateRequest.Contact(contactId, "(+1) 4167483988", contactType, true);
        List<UUID> roleIds =List.of(UUID.randomUUID());
        LocalDate birthdate = LocalDate.of(1998, 2, 12);
        PutUserUpdateRequest request = new PutUserUpdateRequest(
                UUID.randomUUID(),
                "ABCGTDhj23445676",
                "Mati",
                true,
                true,
                true,
                true,
                "Matias",
                "",
                "Mata",
                "M",
                birthdate,
                List.of(contact),
                roleIds
        );
        assertEquals("ABCGTDhj23445676", request.password());
        assertEquals("Mati", request.displayName());
        assertTrue(request.active());
        assertTrue(request.notificationEmail());
        assertTrue(request.notificationSms());
        assertTrue(request.privacyDataOutActive());
        assertEquals("Matias", request.firstName());
        assertEquals("", request.middleName());
        assertEquals("Mata", request.lastName());
        assertEquals("M", request.gender());
        assertEquals(birthdate, request.birthdate());
        assertNotNull(request.contacts());
        assertEquals(1, request.contacts().size());
        assertEquals(contact, request.contacts().get(0));
    }

    @Test
    @DisplayName("Should return correct string representation for Contact")
    void testContactToString() {
        UUID contactId = UUID.randomUUID();
        UUID contactTypeId = UUID.randomUUID();
        PutUserUpdateRequest.ContactType contactType = new PutUserUpdateRequest.ContactType(contactTypeId);
        PutUserUpdateRequest.Contact contact = new PutUserUpdateRequest.Contact(contactId, "(+1) 4167483988", contactType, true);
        String str = contact.toString();
        assertTrue(str.contains("Contact{"));
        assertTrue(str.contains(contactId.toString()));
        assertTrue(str.contains(contactTypeId.toString()));
    }

    @Test
    @DisplayName("Should return correct string representation for ContactType")
    void testContactTypeToString() {
        UUID contactTypeId = UUID.randomUUID();
        PutUserUpdateRequest.ContactType contactType = new PutUserUpdateRequest.ContactType(contactTypeId);
        String str = contactType.toString();
        assertTrue(str.contains("ContactType{"));
        assertTrue(str.contains(contactTypeId.toString()));
    }

    @Test
    @DisplayName("Should return correct string representation for PutUserUpdateRequest")
    void testToString() {
        UUID contactId = UUID.randomUUID();
        UUID contactTypeId = UUID.randomUUID();
        PutUserUpdateRequest.ContactType contactType = new PutUserUpdateRequest.ContactType(contactTypeId);
        PutUserUpdateRequest.Contact contact = new PutUserUpdateRequest.Contact(contactId, "(+1) 4167483988", contactType, true);
        LocalDate birthdate = LocalDate.of(1998, 2, 12);
        PutUserUpdateRequest request = new PutUserUpdateRequest(
                UUID.randomUUID(),
                "ABCGTDhj23445676",
                "Mati",
                true,
                true,
                true,
                true,
                "Matias",
                "",
                "Mata",
                "M",
                birthdate,
                List.of(contact),
                List.of(UUID.randomUUID())
        );
        String str = request.toString();
        assertTrue(str.contains("PutUserUpdateRequest{"));
        assertTrue(str.contains("Mati"));
        assertTrue(str.contains("Matias"));
        assertTrue(str.contains("Mata"));
        assertTrue(str.contains("M"));
        assertTrue(str.contains(birthdate.toString()));
        assertTrue(str.contains(contact.toString()));
    }

    @Test
    @DisplayName("Should handle null contacts list")
    void testNullContacts() {
        PutUserUpdateRequest request = new PutUserUpdateRequest(
                UUID.randomUUID(),
                "pass",
                "disp",
                false,
                false,
                false,
                false,
                "first",
                "mid",
                "last",
                "F",
                LocalDate.now(),
                null,
                List.of(UUID.randomUUID())
        );
        assertNull(request.contacts());
    }
}
