/*
 * @(#)$file.className.java.
 *
 * Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *
 * All rights to this product are owned by Luis Antonio Mata Mata and may only
 * be used under the terms of its associated license document. You may NOT
 * copy, modify, sublicense, or distribute this source file or portions of
 * it unless previously authorized in writing by Luis Antonio Mata Mata.
 * In any event, this notice and the above copyright must always be included
 * verbatim with this file.
 */
package com.prx.backoffice.v1.user.api.controller;

import com.prx.backoffice.util.MessageUtil;
import com.prx.backoffice.v1.user.api.to.UserAccessRequest;
import com.prx.backoffice.v1.user.api.to.UserTO;
import com.prx.backoffice.v1.user.service.UserService;
import com.prx.commons.util.ValidatorCommonsUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/user")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserController {
    private static final String STR_ID_USER = "User Id";

    private final UserService userService;
    private final MessageUtil messageUtil;

//    @PreAuthorize("hasAnyAuthority('ms_user_test')")
    @Operation(description = "Busca los usuarios a través del identificador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "User found.")
//            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "${messages.general.user-find.ok}")
//            ,
//            @ApiResponse(responseCode = "404", description = "${messages.general.user-find.nok}"),
//            @ApiResponse(responseCode = "500", description = "${messages.general.user-find.error}")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/{userId}")
    public ResponseEntity<UserTO> find(@Parameter(description = STR_ID_USER, required = true) @PathVariable @NotNull Long userId){
        return userService.findUserById(userId);
    }

    @Operation(description = "Obtiene una lista de usuarios")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "Usuario encontrado")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/findAll")
    public ResponseEntity<List<UserTO>> findAll(){
        return userService.findAll();
    }

    @Operation(description = "Realiza la autenticación de usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "Usuario encontrado")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/login")
    public ResponseEntity<String> login(@Parameter(description = "Objeto de tipo UserAccessRequest", required = true)
                                    @RequestBody UserAccessRequest userAccessRequest) {
        if(ValidatorCommonsUtil.esNulo(userAccessRequest)){
            return new ResponseEntity<>(messageUtil.getUserSolicitudNulaVacia(), HttpStatus.NOT_ACCEPTABLE);
        }else if(ValidatorCommonsUtil.esVacio(userAccessRequest.getAlias())){
            return new ResponseEntity<>(messageUtil.getUserAliasNuloVacio(), HttpStatus.NOT_ACCEPTABLE);
        }else if(ValidatorCommonsUtil.esVacio(userAccessRequest.getPassword())){
            return new ResponseEntity<>(messageUtil.getUserClaveNulaVacia(), HttpStatus.NOT_ACCEPTABLE);
        }
        return userService.access(userAccessRequest.getAlias(), userAccessRequest.getPassword());
    }

    @Operation(description = "Crea un nuevo usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "Usuario creado con éxito.")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/")
    public ResponseEntity<UserTO> create(@Parameter(description = "Objeto de tipo UserCreateRequest", required = true)
                           @RequestBody UserTO userTO) {
        log.info("{} /create", MessageUtil.LOG_START_MSG);
        if(ValidatorCommonsUtil.esNulo(userTO)){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }else if(ValidatorCommonsUtil.esNulo(userTO.getAlias())
                || ValidatorCommonsUtil.esNulo(userTO.getPassword())){
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
        }
        return userService.create(userTO);
    }

    @Operation(description = "Update a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "Updated user")
    })
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/{userId}")
    public ResponseEntity<UserTO> update(@PathVariable @NotNull Long userId, @RequestBody @NotNull UserTO user) {
        log.info("{} /update/{userId}", MessageUtil.LOG_START_MSG);
        return userService.update(userId, user);
    }

    @Operation(description = "Busca un usuario por un alias")
    @ApiResponses(value = {
            @ApiResponse(responseCode = MessageUtil.OK_VALUE, description = "Usuario encontrado.")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/findByAlias/{alias}")
    public ResponseEntity<UserTO> findByAlias(@Parameter(description = "Alias de usuario", required = true)
                                    @PathVariable @NotNull String alias) {
        return userService.findUserByAlias(alias);
    }

    @Operation(description = "Desvincula un rol de usuario")
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/unlink/{userId}/{roleId}")
    public ResponseEntity<UserTO> unlink(@Parameter(description = "Id de usuario") @PathVariable @NotNull Long userId,
                           @Parameter(description = "Id de rol") @PathVariable @NotNull Long roleId) {
        return userService.unlink(userId, roleId);
    }

    @Operation(description = "Vincula un rol de usuario")
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/link/{userId}/{roleId}")
    public ResponseEntity<UserTO> link(@Parameter(description = STR_ID_USER) @PathVariable @NotNull Long userId,
                         @Parameter(description = "Id de rol") @PathVariable @NotNull Long roleId) {
        return userService.link(userId, roleId);
    }
}
