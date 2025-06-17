package com.prx.backoffice.v1.users.api.controller;

import com.prx.backoffice.v1.users.api.to.PatchUserUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

class UserApiTest {

    @Test
    @DisplayName("Should return ACCEPTED when patchUserDetail is successful")
    void patchUserDetailAccepted() {
        UserController controller = mock(UserController.class);
        Mockito.when(controller.patchUserDetail(any(UUID.class), any(PatchUserUpdateRequest.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.ACCEPTED).build());
        UserApi api = (UserApi) controller;
        ResponseEntity<Void> response = api.patchUserDetail(UUID.randomUUID(), new PatchUserUpdateRequest(
                "12345678",
                "ltnt",
                true,
                true,
                true,
                true,
                "Frodo",
                "Tiny",
                "Bolson",
                "M",
                LocalDate.of(1300, 01, 01),
                List.of(new PatchUserUpdateRequest.Contact(UUID.randomUUID(), "4165895269", new PatchUserUpdateRequest.ContactType(UUID.randomUUID()), true))
        ));
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return NOT_ACCEPTABLE when patchUserDetail is rejected")
    void patchUserDetailRejected() {
        UserController controller = mock(UserController.class);
        Mockito.when(controller.patchUserDetail(any(UUID.class), any(PatchUserUpdateRequest.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build());
        UserApi api = (UserApi) controller;
        ResponseEntity<Void> response = api.patchUserDetail(UUID.randomUUID(), new PatchUserUpdateRequest(
                "12345678",
                "ltnt",
                true,
                true,
                true,
                true,
                "Frodo",
                "Tiny",
                "Bolson",
                "M",
                null,
                null
        ));
        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
    }
}



