package com.prx.backoffice.to.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.prx.commons.to.Request;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

import static com.prx.commons.util.JsonUtil.toJson;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
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

    public String toString(){
        return toJson(this);
    }
}
