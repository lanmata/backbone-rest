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

package com.prx.backoffice.v1.user.api.to;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.prx.commons.pojo.User;
import com.prx.commons.to.Response;
import com.prx.commons.util.JsonUtil;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@JsonInclude
@JsonPropertyOrder({
        "datetimeResponse",
        "list"
})
public class UserListResponse extends Response {
    @JsonProperty("datetimeResponse")
    private LocalDateTime datetimeResponse;
    @JsonProperty("list")
    private List<User> list;

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }
}
