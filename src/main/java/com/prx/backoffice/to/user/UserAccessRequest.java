/*
 *
 *  * @(#)UserAccessRequest.java.
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

package com.prx.backoffice.to.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.prx.commons.to.Request;
import com.prx.commons.util.JsonUtil;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonPropertyOrder({
        "alias",
        "password"
})
public class UserAccessRequest extends Request {
    @NotNull
    @JsonProperty("alias")
    private String alias;
    @NotNull
    @JsonProperty("password")
    private String password;

    @Override
    public String toString(){
        return JsonUtil.toJson(this);
    }
}
