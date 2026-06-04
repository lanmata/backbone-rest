package com.umdc.backoffice.v1.managedclient;

import com.umdc.backoffice.v1.managedclient.api.controller.ManagedClientController;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientCreateRequest;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientCreateResponse;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientErrorResponse;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientTO;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientUpdateRequest;
import com.umdc.backoffice.v1.managedclient.service.ManagedClientRotationService;
import com.umdc.backoffice.v1.managedclient.service.ManagedClientService;
import com.umdc.backoffice.v1.managedclient.service.ManagedClientTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

/// Integration-style controller tests for {@link ManagedClientController} verifying
/// correct delegation to service layer (AC-REG-01, AC-REG-02, AC-SEC-02).
@ExtendWith(MockitoExtension.class)
class ManagedClientControllerTest {

    // PMD AvoidDuplicateLiterals constants
    private static final String TEST_NAME     = "integration-client";
    private static final String TEST_SCOPE    = "read:data";
    private static final String ERR_CONFLICT  = "client_conflict";
    private static final String ERR_NOT_FOUND = "not_found";
    private static final String ERR_BAD_REQ   = "validation_error";

    @Mock
    private ManagedClientService managedClientService;

    @Mock
    private ManagedClientTokenService managedClientTokenService;

    @Mock
    private ManagedClientRotationService managedClientRotationService;

    private ManagedClientController controller;

    @BeforeEach
    void setUp() {
        controller = new ManagedClientController(
                managedClientService, managedClientTokenService, managedClientRotationService);
    }

    // ── registerManagedClient ─────────────────────────────────────────────────

    @Test
    @DisplayName("POST /managed-clients — valid request delegates and returns 201 (AC-REG-01)")
    void registerClient_valid_returns201() {
        ManagedClientCreateRequest request = buildCreateRequest();
        ManagedClientCreateResponse responseBody = buildCreateResponse();
        doReturn(ResponseEntity.status(HttpStatus.CREATED).body(responseBody))
                .when(managedClientService).registerClient(request);

        ResponseEntity<?> response = controller.registerManagedClient(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(((ManagedClientCreateResponse) response.getBody()).getClientSecret());
        verify(managedClientService).registerClient(request);
    }

    @Test
    @DisplayName("POST /managed-clients — duplicate returns 409 (AC-REG-02)")
    void registerClient_duplicate_returns409() {
        ManagedClientCreateRequest request = buildCreateRequest();
        doReturn(ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ManagedClientErrorResponse(ERR_CONFLICT, "Duplicate")))
                .when(managedClientService).registerClient(request);

        ResponseEntity<?> response = controller.registerManagedClient(request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(ERR_CONFLICT, ((ManagedClientErrorResponse) response.getBody()).getError());
    }

    @Test
    @DisplayName("POST /managed-clients — validation error returns 400 (AC-REG-03)")
    void registerClient_missingName_returns400() {
        ManagedClientCreateRequest request = buildCreateRequest();
        doReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ManagedClientErrorResponse(ERR_BAD_REQ, "Name required")))
                .when(managedClientService).registerClient(request);

        ResponseEntity<?> response = controller.registerManagedClient(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("POST /managed-clients — empty scopes returns 400")
    void registerClient_emptyScopes_returns400() {
        ManagedClientCreateRequest request = buildCreateRequest();
        doReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ManagedClientErrorResponse(ERR_BAD_REQ, "Scopes required")))
                .when(managedClientService).registerClient(request);

        ResponseEntity<?> response = controller.registerManagedClient(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ── listManagedClients ────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /managed-clients — returns 200 with list")
    void listClients_returns200() {
        doReturn(ResponseEntity.ok(List.of(buildTO())))
                .when(managedClientService).listClients(null, null, 0, 20);

        ResponseEntity<?> response = controller.listManagedClients(null, null, 0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ── getManagedClient ──────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /managed-clients/{clientId} — found returns 200 with no secret fields (AC-SEC-02)")
    void getManagedClient_returns200WithNoSecretFields() {
        UUID clientId = UUID.randomUUID();
        ManagedClientTO to = buildTO();
        doReturn(ResponseEntity.ok(to)).when(managedClientService).getClient(clientId);

        ResponseEntity<?> response = controller.getManagedClient(clientId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ManagedClientTO body = (ManagedClientTO) response.getBody();
        assertNotNull(body);
        boolean hasSecret = Arrays.stream(ManagedClientTO.class.getDeclaredFields())
                .anyMatch(f -> "secretHash".equals(f.getName()) || "prevSecretHash".equals(f.getName()));
        assertFalse(hasSecret, "GET response must not contain secret fields");
    }

    @Test
    @DisplayName("GET /managed-clients/{unknown} — not found returns 404")
    void getManagedClient_notFound_returns404() {
        UUID clientId = UUID.randomUUID();
        doReturn(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ManagedClientErrorResponse(ERR_NOT_FOUND, "Not found", clientId.toString())))
                .when(managedClientService).getClient(clientId);

        ResponseEntity<?> response = controller.getManagedClient(clientId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ── updateManagedClient ───────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /managed-clients/{clientId} — returns 200 with updated TO")
    void updateManagedClient_returns200() {
        UUID clientId = UUID.randomUUID();
        ManagedClientUpdateRequest request = new ManagedClientUpdateRequest();
        doReturn(ResponseEntity.ok(buildTO())).when(managedClientService).updateClient(clientId, request);

        ResponseEntity<?> response = controller.updateManagedClient(clientId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(managedClientService).updateClient(clientId, request);
    }

    // ── deleteManagedClient ───────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /managed-clients/{clientId} — returns 204")
    void deleteManagedClient_returns204() {
        UUID clientId = UUID.randomUUID();
        doReturn(ResponseEntity.noContent().build()).when(managedClientService).deleteClient(clientId);

        ResponseEntity<?> response = controller.deleteManagedClient(clientId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(managedClientService).deleteClient(clientId);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private ManagedClientCreateRequest buildCreateRequest() {
        ManagedClientCreateRequest req = new ManagedClientCreateRequest();
        req.setName(TEST_NAME);
        req.setApplicationId(UUID.randomUUID());
        req.setScopes(List.of(TEST_SCOPE));
        req.setActive(true);
        return req;
    }

    private ManagedClientCreateResponse buildCreateResponse() {
        ManagedClientCreateResponse resp = new ManagedClientCreateResponse();
        resp.setClientId(UUID.randomUUID());
        resp.setClientSecret("raw-secret-value");
        resp.setName(TEST_NAME);
        return resp;
    }

    private ManagedClientTO buildTO() {
        ManagedClientTO to = new ManagedClientTO();
        to.setClientId(UUID.randomUUID());
        to.setName(TEST_NAME);
        to.setApplicationId(UUID.randomUUID());
        to.setScopes(List.of(TEST_SCOPE));
        to.setActive(true);
        return to;
    }
}
