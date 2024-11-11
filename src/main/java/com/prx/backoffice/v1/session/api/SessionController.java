package com.prx.backoffice.v1.session.api;

import com.prx.backoffice.util.MessageUtil;
import com.prx.backoffice.v1.session.services.SessionJwtService;
import com.prx.backoffice.v1.session.to.SessionTokenResponse;
import com.prx.backoffice.v1.users.api.to.UserAccessRequest;
import com.prx.backoffice.v1.users.service.UserService;
import com.prx.commons.util.ValidatorCommonsUtil;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;

import static com.prx.backoffice.v1.session.services.SessionJwtService.SESSION_KEY;

@RestController
@RequestMapping("/v1/session")
public class SessionController {

    private final SessionJwtService sessionJwtService;
    private final UserService userService;
    private final MessageUtil messageUtil;

    public SessionController(SessionJwtService sessionJwtService, UserService userService, MessageUtil messageUtil) {
        this.sessionJwtService = sessionJwtService;
        this.userService = userService;
        this.messageUtil = messageUtil;
    }

    @PostMapping("/token")
    public ResponseEntity<SessionTokenResponse> generateSessionToken(@RequestBody UserAccessRequest userAccessRequest) {
        String sessionId = UUID.randomUUID().toString();
        String sessionToken;
        ResponseEntity<SessionTokenResponse> responseEntity;
        boolean isFieldsInvalid = false;
        String messageError = "";
        ResponseEntity<String> userResponse;

        if (ValidatorCommonsUtil.esNulo(userAccessRequest)) {
            messageError = messageUtil.getUserSolicitudNulaVacia();
            isFieldsInvalid = true;
        } else if (ValidatorCommonsUtil.esVacio(userAccessRequest.getAlias())) {
            messageError = messageUtil.getUserAliasNuloVacio();
            isFieldsInvalid = true;
        } else if (ValidatorCommonsUtil.esVacio(userAccessRequest.getPassword())) {
            messageError = messageUtil.getUserClaveNulaVacia();
            isFieldsInvalid = true;
        }

        if (isFieldsInvalid) {
            responseEntity = new ResponseEntity<>(new SessionTokenResponse(messageError), HttpStatus.NOT_ACCEPTABLE);
            return responseEntity;
        }

        userResponse = userService.access(userAccessRequest.getAlias(), userAccessRequest.getPassword());
        if (Objects.nonNull(userResponse) && userResponse.getStatusCode().equals(HttpStatus.ACCEPTED)) {
            sessionToken = sessionJwtService.generateSessionToken(sessionId);
            return ResponseEntity.ok(new SessionTokenResponse(sessionToken));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateSessionToken(
            @RequestHeader(SESSION_KEY) String sessionToken) {
        boolean isValid = false;
        try {
            var value = sessionJwtService.getTokenClaims(sessionToken).get("type");
            isValid = SESSION_KEY.equals(value) && !sessionJwtService.isTokenExpired(sessionToken);

        } catch (ExpiredJwtException e) {
            return ResponseEntity.ok(false);
        }
        return ResponseEntity.ok(isValid);
    }
}
