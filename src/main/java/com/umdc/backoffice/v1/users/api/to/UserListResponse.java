/*
 *  @(#)UserListResponse.java
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

package com.umdc.backoffice.v1.users.api.to;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.prx.commons.general.pojo.User;
import com.prx.commons.general.to.Response;
import java.time.LocalDateTime;
import java.util.List;

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

    /**
     * Default Constructor.
     */
    public UserListResponse() {
        super();
        // Default Constructor.
    }

    public LocalDateTime getDatetimeResponse() {
        return datetimeResponse;
    }

    public void setDatetimeResponse(LocalDateTime datetimeResponse) {
        this.datetimeResponse = datetimeResponse;
    }

    public List<User> getList() {
        return list;
    }

    public void setList(List<User> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "UserListResponse{" +
                "datetimeResponse=" + datetimeResponse +
                ", list=" + list +
                '}';
    }
}
