package com.umdc.backoffice.v1.managedclient.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Unit tests for {@link ManagedClientSecretHashServiceImpl} covering BCrypt
/// hashing correctness and constant-time matching (AC-SEC-01).
class ManagedClientSecretHashServiceImplTest {

    // BCrypt strength 4 keeps tests fast while preserving algorithm correctness
    private static final int   BCRYPT_STRENGTH = 4;
    private static final String RAW_SECRET     = "my-test-raw-secret";
    private static final String WRONG_SECRET   = "wrong-secret";

    private ManagedClientSecretHashServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ManagedClientSecretHashServiceImpl(new BCryptPasswordEncoder(BCRYPT_STRENGTH));
    }

    @Test
    @DisplayName("hashSecret — produces valid BCrypt hash starting with $2a$ or $2b$")
    void hashSecret_producesValidBcryptHash() throws Exception {
        CompletableFuture<String> future = service.hashSecret(RAW_SECRET);
        String hash = future.get();

        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"),
                "Expected BCrypt prefix but got: " + hash);
    }

    @Test
    @DisplayName("hashSecret — raw secret is NOT contained in the hash string (AC-SEC-01)")
    void hashSecret_rawSecretNotInHashString() throws Exception {
        CompletableFuture<String> future = service.hashSecret(RAW_SECRET);
        String hash = future.get();

        assertFalse(hash.contains(RAW_SECRET),
                "Hash must not contain the plaintext secret");
    }

    @Test
    @DisplayName("matchesWithConstantTime — correct secret returns true")
    void matchesWithConstantTime_correctSecret_returnsTrue() throws Exception {
        String hash = service.hashSecret(RAW_SECRET).get();

        assertTrue(service.matchesWithConstantTime(RAW_SECRET, hash));
    }

    @Test
    @DisplayName("matchesWithConstantTime — wrong secret returns false")
    void matchesWithConstantTime_wrongSecret_returnsFalse() throws Exception {
        String hash = service.hashSecret(RAW_SECRET).get();

        assertFalse(service.matchesWithConstantTime(WRONG_SECRET, hash));
    }

    @Test
    @DisplayName("hashSecret — returns a CompletableFuture that is done without exception")
    void hashSecret_isAsync() throws Exception {
        CompletableFuture<String> future = service.hashSecret(RAW_SECRET);

        assertNotNull(future);
        // In a unit test without Spring async context, @Async executes synchronously
        assertTrue(future.isDone(), "CompletableFuture must complete");
        assertFalse(future.isCompletedExceptionally(), "CompletableFuture must not complete exceptionally");
    }
}
