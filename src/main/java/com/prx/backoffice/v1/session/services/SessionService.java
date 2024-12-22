/*
 *  @(#)SessionService.java
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
