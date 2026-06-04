/*
 *  @(#)ManagedClientApi.java
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
package com.umdc.backoffice.v1.managedclient.api.controller;

import com.umdc.backoffice.v1.iam.tokens.api.to.TokenIntrospectRequest;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientCreateRequest;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientTokenRequest;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientUpdateRequest;
import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

/// REST API interface for the Management Client Authentication Manager (MCAM) CRUD operations.
@Tag(name = "managed-clients", description = "Management Client Authentication Manager — M2M credential lifecycle")
@RequestMapping("/api/v1/managed-clients")
public interface ManagedClientApi {

    @Operation(summary = "Register a new managed client",
               description = "Returns the clientSecret exactly once in the response body.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Client registered"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "409", description = "Duplicate name for application")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<?> registerManagedClient(
            @Valid @RequestBody ManagedClientCreateRequest request) {
        return ((ManagedClientController) this).getManagedClientService().registerClient(request);
    }

    @Operation(summary = "List managed clients")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Client list"),
        @ApiResponse(responseCode = "204", description = "No clients found")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<?> listManagedClients(
            @RequestParam(required = false) UUID applicationId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ((ManagedClientController) this).getManagedClientService()
                .listClients(applicationId, active, page, size);
    }

    @Operation(summary = "Get a managed client by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Client found"),
        @ApiResponse(responseCode = "404", description = "Client not found")
    })
    @GetMapping(value = "/{clientId}", produces = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<?> getManagedClient(@PathVariable UUID clientId) {
        return ((ManagedClientController) this).getManagedClientService().getClient(clientId);
    }

    @Operation(summary = "Update a managed client")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Client updated"),
        @ApiResponse(responseCode = "404", description = "Client not found")
    })
    @PutMapping(value = "/{clientId}", produces = MediaType.APPLICATION_JSON_VALUE,
                consumes = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<?> updateManagedClient(
            @PathVariable UUID clientId,
            @Valid @RequestBody ManagedClientUpdateRequest request) {
        return ((ManagedClientController) this).getManagedClientService()
                .updateClient(clientId, request);
    }

    @Operation(summary = "Delete a managed client")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Client deleted"),
        @ApiResponse(responseCode = "404", description = "Client not found")
    })
    @DeleteMapping(value = "/{clientId}")
    default ResponseEntity<?> deleteManagedClient(@PathVariable UUID clientId) {
        return ((ManagedClientController) this).getManagedClientService().deleteClient(clientId);
    }

    @Operation(summary = "Issue an M2M access token",
               description = "Client credentials grant — public endpoint, no bearer token required.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Token issued"),
        @ApiResponse(responseCode = "400", description = "Invalid scope"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    @PostMapping(value = "/token", produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<?> issueToken(@Valid @RequestBody ManagedClientTokenRequest request) {
        return ((ManagedClientController) this).getManagedClientTokenService().issueToken(request);
    }

    @Operation(summary = "Revoke all tokens for a managed client")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "All tokens revoked"),
        @ApiResponse(responseCode = "404", description = "Client not found")
    })
    @DeleteMapping(value = "/{clientId}/tokens")
    default ResponseEntity<?> revokeAllTokens(@PathVariable UUID clientId) {
        return ((ManagedClientController) this).getManagedClientTokenService().revokeAllTokens(clientId);
    }

    @Operation(summary = "Introspect an M2M token",
               description = "Always returns HTTP 200. active=false for invalid/expired/revoked tokens.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Introspection result")
    })
    @PostMapping(value = "/introspect", produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<?> introspectToken(@Valid @RequestBody TokenIntrospectRequest request) {
        return ((ManagedClientController) this).getManagedClientTokenService().introspectToken(request.token());
    }

    @Operation(summary = "Rotate the secret for a managed client",
               description = "Returns the new plaintext clientSecret once. Grace period allows old secret temporarily.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Secret rotated"),
        @ApiResponse(responseCode = "404", description = "Client not found or inactive")
    })
    @PostMapping(value = "/{clientId}/rotate-secret",
                 produces = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<?> rotateManagedClientSecret(
            @PathVariable UUID clientId,
            HttpServletRequest httpRequest) {
        return ((ManagedClientController) this).getManagedClientRotationService()
                .rotateSecret(clientId, httpRequest.getRemoteAddr());
    }
}
