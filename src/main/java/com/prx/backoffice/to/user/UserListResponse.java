package com.prx.backoffice.to.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.prx.commons.pojo.User;
import com.prx.commons.to.Response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.prx.commons.util.JsonUtil.toJson;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude
@JsonPropertyOrder({
        "datetimeResponse",
        "list"
})
public class UserListResponse extends Response {
    @JsonProperty("datetimeResponse")
    private String datetimeResponse;
    @JsonProperty("list")
    private List<User> list;

    @Override
    public String toString() {
        return toJson(this);
    }
}
