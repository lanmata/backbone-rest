package com.prx.backoffice.v1.session.to;

public class SessionTokenResponse {
    private String token;

    public SessionTokenResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
