package com.umdc.backoffice.messages;

import com.umdc.commons.constants.httpstatus.type.MessageType;

public enum JWTMessage implements MessageType {
    TOKEN_EXPIRED(401, "Refresh token has expired beyond the grace period."),
    TOKEN_INVALID(401, "Invalid or malformed token."),
    TOKEN_MISSING(400, "Token missing"),
    CANNOT_IDENTIFY_USER(401, "Cannot identify user from token claims"),
    USER_INACTIVE(401, "User not found or account is inactive");

    private final int code;
    private final String status;

    JWTMessage(int code, String status) {
        this.code = code;
        this.status = status;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getCodeToString() {
        return String.valueOf(code);
    }

    @Override
    public String getStatus() {
        return status;
    }
}
