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

import com.prx.backoffice.to.user.DateRequest;
import com.prx.backoffice.to.user.UserAccessRequest;
import com.prx.backoffice.to.user.UserAccessResponse;
import com.prx.backoffice.to.user.UserCreateRequest;
import com.prx.backoffice.to.user.UserListResponse;
import com.prx.backoffice.to.user.UserResponse;
import com.prx.backoffice.util.MessageUtil;
import com.prx.commons.pojo.MessageActivity;
import com.prx.commons.pojo.User;
import com.prx.commons.to.Response;
import static com.prx.commons.util.DateUtil.DATE_FORMATTER;
import static com.prx.commons.util.MessageActivityUtil.toResponse;
import com.prx.commons.util.ValidatorCommonsUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.time.LocalDateTime;
import java.time.ZoneId;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/user")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserController {
    private final com.prx.backoffice.service.UserService userService;
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
        UserResponse userResponse;
        MessageActivity<User> messageActivity;

        userResponse = new UserResponse();
        messageActivity = userService.findUserById(userId);
        MessageActivityUtil.toResponse(messageActivity, userResponse);
        userResponse.setUser(messageActivity.getObjectResponse());

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
        final var messageActivity = userService.findAll();
        final var userListResponse = new UserListResponse();

        userListResponse.setList(messageActivity.getObjectResponse());
        userListResponse.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));
        userListResponse.setMessage(messageActivity.getMessages().get(1));

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
    @RequestBody UserAccessRequest userAccessRequest){
        UserAccessResponse userAccessResponse = new UserAccessResponse();
        MessageActivity messageActivity;

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
            // TODO - Agregar logica para validar datos de usuario
            messageActivity = userService.access(userAccessRequest.getAlias(), userAccessRequest.getPassword());
            userAccessResponse.setToken(messageActivity.getObjectResponse().toString());
            MessageActivityUtil.toResponse(messageActivity, userAccessResponse);
        }
        userAccessResponse.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));

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
    @RequestBody UserCreateRequest userCreateRequest){
        final var response = new Response();

        if(ValidatorCommonsUtil.esNulo(userCreateRequest)||ValidatorCommonsUtil.esNulo(userCreateRequest.getUser())){
            response.setMessage(messageUtil.getUserSolicitudNulaVacia());
        }else if(ValidatorCommonsUtil.esNulo(userCreateRequest.getUser().getAlias())){
            response.setMessage(messageUtil.getUserAliasNuloVacio());
        }else if(ValidatorCommonsUtil.esNulo(userCreateRequest.getUser().getPassword())){
            response.setMessage(messageUtil.getUserClaveNulaVacia());
        }

        MessageActivityUtil.toResponse(userService.create(userCreateRequest.getUser()), response);

        return response;
    }

}
