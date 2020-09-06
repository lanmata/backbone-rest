package com.prx.backoffice.controller;

import com.prx.backoffice.model.UserModel;
import com.prx.backoffice.to.user.*;
import com.prx.backoffice.util.MessageUtil;
import com.prx.commons.pojo.MessageActivity;
import com.prx.commons.pojo.User;
import com.prx.commons.to.Response;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

import static com.prx.commons.util.MessageActivityUtil.toResponse;
import static com.prx.commons.util.ValidatorCommons.esNulo;
import static com.prx.commons.util.ValidatorCommons.esVacio;
import static java.time.LocalDateTime.now;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@CrossOrigin(origins = "*")
@RequestMapping("/user")
@RestController
public class UserController {
    @Autowired
    private UserModel userModel;
    @Autowired
    private MessageUtil messageUtil;

    @PreAuthorize("hasAnyAuthority('ms_user_test')")
    @ApiOperation(value = "Valor identificador",
            notes = "Provee el ultimo dato del mercado")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = UserResponse.class)
            ,@ApiResponse(code = 404, message = "No encontrado", response = UserResponse.class)
            ,@ApiResponse(code = 500, message = "Internal Server Error", response = String.class)
    })
    @GetMapping(produces = APPLICATION_JSON_VALUE, path = "/find/{token}/{userId}")
    public UserResponse find(@ApiParam(value = "Token de acceso", required = true) @PathVariable @NotNull String token,
                             @ApiParam(value = "Id de usuario", required = true) @PathVariable @NotNull Long userId){
        UserResponse userResponse;
        MessageActivity messageActivity;

        userResponse = new UserResponse();
        messageActivity = userModel.findUserById(userId);
        toResponse(messageActivity, userResponse);
        userResponse.setUser((User) messageActivity.getObjectResponse());

        return userResponse;
    }

    @ApiOperation(value = "Obtiene una lista de usuarios",
            notes = "Provee el ultimo dato del mercado")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = UserListResponse.class)
            ,@ApiResponse(code = 404, message = "No encontrado", response = UserListResponse.class)
            ,@ApiResponse(code = 500, message = "Internal Server Error", response = String.class)
    })
    @GetMapping(produces = APPLICATION_JSON_VALUE, path = "/findAll/{token}/{userId}")
    public UserListResponse findAll(@ApiParam(value = "Token de acceso", required = true) @PathVariable @NotNull String token,
                                    @ApiParam(value = "Id de usuario", required = true) @PathVariable @NotNull Long userId){
        UserListResponse userResponse;

        userResponse = new UserListResponse();
        userResponse.setList(userModel.findAll());
        userResponse.setDatetimeResponse(now().toString());

        return userResponse;
    }

    @ApiOperation(value = "Realiza la autenticacion de usuario",
            notes = "Provee el ultimo dato del mercado")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = UserAccessResponse.class)
            ,@ApiResponse(code = 401, message = "No autorizado", response = UserAccessResponse.class)
            ,@ApiResponse(code = 404, message = "No encontrado", response = UserAccessResponse.class)
            ,@ApiResponse(code = 500, message = "Internal Server Error", response = String.class)
    })
    @PostMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE, path = "/login")
    public UserAccessResponse login(@ApiParam(value = "Objeto de tipo UserAccessRequest", required = true)
                                    @RequestBody UserAccessRequest userAccessRequest){
        UserAccessResponse userAccessResponse = new UserAccessResponse();
        MessageActivity messageActivity;

        if(esNulo(userAccessRequest)){
            userAccessResponse.setCode(401);
            userAccessResponse.setMessage(messageUtil.getUserSolicitudNulaVacia());
        }else if(esVacio(userAccessRequest.getAlias())){
            userAccessResponse.setCode(401);
            userAccessResponse.setMessage(messageUtil.getUserAliasNuloVacio());
        }else if(esVacio(userAccessRequest.getPassword())){
            userAccessResponse.setCode(401);
            userAccessResponse.setMessage(messageUtil.getUserClaveNulaVacia());
        }else{
            // TODO - Agregar logica para validar datos de usuario
            messageActivity = userModel.access(userAccessRequest.getAlias(), userAccessRequest.getPassword());
            userAccessResponse.setToken(messageActivity.getObjectResponse().toString());
            toResponse(messageActivity, userAccessResponse);
        }
        userAccessResponse.setDateTime(now().toString());

        return userAccessResponse;
    }

    @ApiOperation(value = "Crea un nuevo usuario",
            notes = "Realiza la creacion de usuario para ingreso en el sistema")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Response.class)
            ,@ApiResponse(code = 401, message = "No autorizado", response = Response.class)
            ,@ApiResponse(code = 404, message = "No encontrado", response = Response.class)
            ,@ApiResponse(code = 500, message = "Internal Server Error", response = String.class)
    })
    @PostMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE, path = "/create")
    public Response create(@ApiParam(value = "Objeto de tipo UserCreateRequest", required = true)
                               @RequestBody UserCreateRequest userCreateRequest){
        Response response = new Response();

        if(esNulo(userCreateRequest)||esNulo(userCreateRequest.getUser())){
            response.setMessage(messageUtil.getUserSolicitudNulaVacia());
        }else if(esNulo(userCreateRequest.getUser().getAlias())){
            response.setMessage(messageUtil.getUserAliasNuloVacio());
        }else if(esNulo(userCreateRequest.getUser().getPassword())){
            response.setMessage(messageUtil.getUserClaveNulaVacia());
        }

        toResponse(userModel.create(userCreateRequest.getUser()), response);

        return response;
    }
}
