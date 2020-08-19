package com.prx.backoffice.to.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.prx.commons.pojo.User;
import com.prx.commons.to.Request;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import static com.prx.commons.util.JsonUtil.toJson;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({
        "user"
})
public class UserCreateRequest extends Request {
    @JsonProperty("user")
    private User user;

    @Override
    public String toString() {
        return toJson(this);
    }
}
