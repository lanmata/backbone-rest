/*
 *
 *  * @(#)UserController.java.
 *  *
 *  * Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *  *
 *  * All rights to this product are owned by Luis Antonio Mata Mata and may only
 *  * be used under the terms of its associated license document. You may NOT
 *  * copy, modify, sublicense, or distribute this source file or portions of
 *  * it unless previously authorized in writing by Luis Antonio Mata Mata.
 *  * In any event, this notice and the above copyright must always be included
 *  * verbatim with this file.
 *
 */

package com.prx.backoffice.controller;

import com.prx.backoffice.service.UserService;
import com.prx.backoffice.to.user.*;
import com.prx.backoffice.util.MessageUtil;
import com.prx.commons.pojo.MessageActivity;
import com.prx.commons.pojo.User;
import com.prx.commons.to.Response;
import com.prx.commons.util.MessageActivityUtil;
import com.prx.commons.util.ValidatorCommonsUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@RestController
@RequestMapping("/v1/user")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final MessageUtil messageUtil;

    @PreAuthorize("hasAnyAuthority('ms_user_test')")
    @ApiOperation(value = "Busca los usuarios a trav&eacute;s del identificador",
            notes = "Busca los usuarios a trav&eacute;s del identificador")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "${messages.general.user-find.ok}", response = UserResponse.class),
            @ApiResponse(code = 404, message = "${messages.general.user-find.nok}", response = UserResponse.class),
            @ApiResponse(code = 500, message = "${messages.general.user-find.error}", response = String.class)
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/find/{token}/{userId}")
    public UserResponse find(
            @ApiParam(value = "Token de acceso", required = true) @PathVariable @NotNull String token,
            @ApiParam(value = "Id de usuario", required = true) @PathVariable @NotNull Long userId){
        log.info("Inicia llamado al metodo find");
        UserResponse userResponse;
        MessageActivity<User> messageActivity;
        userResponse = new UserResponse();
        messageActivity = userService.findUserById(userId);
        MessageActivityUtil.toResponse(messageActivity, userResponse);
        userResponse.setUser(messageActivity.getObjectResponse());
        log.info("Termina llamado al metodo find");
        return userResponse;
    }

    @ApiOperation(value = "Obtiene una lista de usuarios",
            notes = "Obtiene una lista de usuarios")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "${messages.general.user-find.ok}", response = UserListResponse.class),
            @ApiResponse(code = 404, message = "${messages.general.user-find.nok}", response = UserListResponse.class),
            @ApiResponse(code = 500, message = "${messages.general.user-find.error}", response = String.class)
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/findAll/{token}/{userId}")
    public UserListResponse findAll(
            @ApiParam(value = "Token de acceso", required = true) @PathVariable @NotNull String token,
            @ApiParam(value = "Id de usuario", required = true) @PathVariable @NotNull Long userId){
        log.info("Inicia llamado al metodo findAll");
        final var messageActivity = userService.findAll();
        final var userListResponse = new UserListResponse();
        userListResponse.setList(messageActivity.getObjectResponse());
        userListResponse.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));
        userListResponse.setCode(messageActivity.getCode());
        userListResponse.setMessage(messageActivity.getMessage());
        log.info("Termina llamado al metodo findAll");
        return userListResponse;
    }

    @ApiOperation(value = "Realiza la autenticaci&oacute;n de usuario",
            notes = "Realiza la autenticaci&oacute;n de usuario")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "${messages.general.user-find.ok}", response = UserAccessResponse.class),
            @ApiResponse(code = 401, message = "${messages.general.user-find.not-authorized}", response = UserAccessResponse.class),
            @ApiResponse(code = 404, message = "${messages.general.user-find.nok}", response = UserAccessResponse.class),
            @ApiResponse(code = 500, message = "${messages.general.user-find.error}", response = String.class)
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/login")
    public UserAccessResponse login(@ApiParam(value = "Objeto de tipo UserAccessRequest", required = true)
                                    @RequestBody UserAccessRequest userAccessRequest) {
        log.info("Inicia llamado al metodo login");
        UserAccessResponse userAccessResponse = new UserAccessResponse();
        MessageActivity<String> messageActivity;
        if(ValidatorCommonsUtil.esNulo(userAccessRequest)){
            userAccessResponse.setCode(401);
            userAccessResponse.setMessage(messageUtil.getUserSolicitudNulaVacia());
        }else if(ValidatorCommonsUtil.esVacio(userAccessRequest.getAlias())){
            userAccessResponse.setCode(401);
            userAccessResponse.setMessage(messageUtil.getUserAliasNuloVacio());
        }else if(ValidatorCommonsUtil.esVacio(userAccessRequest.getPassword())){
            userAccessResponse.setCode(401);
            userAccessResponse.setMessage(messageUtil.getUserClaveNulaVacia());
        }else{
            messageActivity = userService.access(userAccessRequest.getAlias(), userAccessRequest.getPassword());
            userAccessResponse.setToken(messageActivity.getObjectResponse());
            MessageActivityUtil.toResponse(messageActivity, userAccessResponse);
        }
        userAccessResponse.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));
        log.info("Termina llamado al metodo login");
        return userAccessResponse;
    }

    @ApiOperation(value = "Crea un nuevo usuario",
            notes = "Crea un nuevo usuario")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Usuario creado con éxito.", response = Response.class),
            @ApiResponse(code = 401, message = "Acción NO autorizada", response = Response.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = String.class)
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/create")
    public Response create(@ApiParam(value = "Objeto de tipo UserCreateRequest", required = true)
                           @RequestBody UserCreateRequest userCreateRequest) {
        log.info("Inicia llamado al metodo create");
        final var response = new Response();
        if(ValidatorCommonsUtil.esNulo(userCreateRequest)||ValidatorCommonsUtil.esNulo(userCreateRequest.getUser())){
            response.setMessage(messageUtil.getUserSolicitudNulaVacia());
        }else if(ValidatorCommonsUtil.esNulo(userCreateRequest.getUser().getAlias())){
            response.setMessage(messageUtil.getUserAliasNuloVacio());
        }else if(ValidatorCommonsUtil.esNulo(userCreateRequest.getUser().getPassword())){
            response.setMessage(messageUtil.getUserClaveNulaVacia());
        }
        MessageActivityUtil.toResponse(userService.create(userCreateRequest.getUser()), response);
        log.info("Termina llamado al metodo create");
        return response;
    }

    @ApiOperation(value = "Busca un usuario por un alias")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Usuario encontrado.", response = Response.class),
            @ApiResponse(code = 401, message = "Acción buscar usuario por alias NO autorizada", response = Response.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = String.class)
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/findByAlias/{alias}")
    public UserResponse findByAlias(@ApiParam(value = "Alias de usuario", required = true, readOnly = true)
                                    @PathVariable @NotNull String alias) {
        log.info("Inicia llamado al metodo findByAlias");
        final var userResponse = new UserResponse();
        final var messageActivity = userService.findUserByAlias(alias);
        userResponse.setUser(messageActivity.getObjectResponse());
        userResponse.setCode(messageActivity.getCode());
        userResponse.setMessage(messageActivity.getMessage());
        userResponse.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));
        log.info("Termina llamado al metodo findByAlias");
        return userResponse;
    }

}
