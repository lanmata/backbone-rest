package com.prx.backoffice.to.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.prx.commons.to.Response;
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
        "token"
})
public class UserAccessResponse extends Response {
    @JsonProperty("token")
    private String token;

    @Override
    public String toString() {
        return toJson(this);
    }
}
