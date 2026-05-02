package com.umdc.backoffice.util;

import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    @Test
    void testGetUidFromToken_NullToken() {
        UUID result = JwtUtil.getUidFromToken(null);
        assertNull(result);
    }

    @Test
    void testGetUidFromToken_BlankToken() {
        UUID result = JwtUtil.getUidFromToken("");
        assertNull(result);
    }

    @Test
    void testGetUidFromToken_EmptyToken() {
        UUID result = JwtUtil.getUidFromToken("   ");
        assertNull(result);
    }

    @Test
    void testGetUidFromToken_InvalidFormat() {
        UUID result = JwtUtil.getUidFromToken("invalid-token");
        assertNull(result);
    }

    @Test
    void testGetUidFromToken_OnlyOnePart() {
        UUID result = JwtUtil.getUidFromToken("singlepart");
        assertNull(result);
    }

    @Test
    void testGetUidFromToken_ValidTokenWithUid() {
        UUID testUuid = UUID.randomUUID();
        String payload = String.format("{\"uid\":\"%s\",\"sub\":\"user\"}", testUuid);
        String encodedPayload = Base64.getUrlEncoder().encodeToString(payload.getBytes());
        String token = "header." + encodedPayload + ".signature";

        UUID result = JwtUtil.getUidFromToken(token);

        assertEquals(testUuid, result);
    }

    @Test
    void testGetUidFromToken_ValidTokenWithoutUid() {
        String payload = "{\"sub\":\"user\",\"role\":\"admin\"}";
        String encodedPayload = Base64.getUrlEncoder().encodeToString(payload.getBytes());
        String token = "header." + encodedPayload + ".signature";

        UUID result = JwtUtil.getUidFromToken(token);

        assertNull(result);
    }

    @Test
    void testGetUidFromToken_InvalidUuidFormat() {
        String payload = "{\"uid\":\"not-a-valid-uuid\"}";
        String encodedPayload = Base64.getUrlEncoder().encodeToString(payload.getBytes());
        String token = "header." + encodedPayload + ".signature";

        UUID result = JwtUtil.getUidFromToken(token);

        assertNull(result);
    }

    @Test
    void testGetUidFromToken_MalformedJson() {
        String payload = "{\"uid\":invalid-json}";
        String encodedPayload = Base64.getUrlEncoder().encodeToString(payload.getBytes());
        String token = "header." + encodedPayload + ".signature";

        UUID result = JwtUtil.getUidFromToken(token);

        assertNull(result);
    }

    @Test
    void testGetUidFromToken_InvalidBase64() {
        String token = "header.invalid-base64!@#$.signature";

        UUID result = JwtUtil.getUidFromToken(token);

        assertNull(result);
    }

    @Test
    void testGetUidFromToken_EmptyPayload() {
        String payload = "{}";
        String encodedPayload = Base64.getUrlEncoder().encodeToString(payload.getBytes());
        String token = "header." + encodedPayload + ".signature";

        UUID result = JwtUtil.getUidFromToken(token);

        assertNull(result);
    }

    @Test
    void testGetUidFromToken_NullUidValue() {
        String payload = "{\"uid\":null}";
        String encodedPayload = Base64.getUrlEncoder().encodeToString(payload.getBytes());
        String token = "header." + encodedPayload + ".signature";

        UUID result = JwtUtil.getUidFromToken(token);

        assertNull(result);
    }

    @Test
    void testGetUidFromToken_ComplexPayload() {
        UUID testUuid = UUID.randomUUID();
        String payload = String.format("{\"sub\":\"user\",\"uid\":\"%s\",\"role\":\"admin\",\"exp\":1234567890}", testUuid);
        String encodedPayload = Base64.getUrlEncoder().encodeToString(payload.getBytes());
        String token = "header." + encodedPayload + ".signature";

        UUID result = JwtUtil.getUidFromToken(token);

        assertEquals(testUuid, result);
    }
}

