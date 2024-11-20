package com.prx.backoffice.v1.session.services;

import com.prx.backoffice.v1.session.to.SessionRequest;
import com.prx.backoffice.v1.session.to.SessionResponse;
import com.prx.backoffice.v1.session.to.UserAliasTO;
import com.prx.security.SessionJwtService;
import org.springframework.http.ResponseEntity;

public interface SessionService extends SessionJwtService {

    ResponseEntity<SessionResponse> loadSession(SessionRequest sessionRequest);

//    ResponseEntity<UserAliasTO> validateUser(String alias);
}
