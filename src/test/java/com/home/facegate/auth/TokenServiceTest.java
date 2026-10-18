package com.home.facegate.auth;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private static final String SECRET = "test-secret-0123456789abcdef0123456789abcdef";

    @Test
    void issuedTokenIsValid() {
        TokenService tokens = new TokenService(SECRET, 60);
        String token = tokens.issue("nicola");
        assertTrue(tokens.isValid(token));
    }

    @Test
    void garbageTokenIsInvalid() {
        TokenService tokens = new TokenService(SECRET, 60);
        assertFalse(tokens.isValid("not-a-jwt"));
    }

    @Test
    void expiredTokenIsInvalid() {
        TokenService tokens = new TokenService(SECRET, -1);
        String token = tokens.issue("nicola");
        assertFalse(tokens.isValid(token));
    }

    @Test
    void tokenSignedWithDifferentSecretIsInvalid() {
        String token = new TokenService(SECRET, 60).issue("nicola");
        TokenService otherKeys = new TokenService("another-secret-0123456789abcdef012345", 60);
        assertFalse(otherKeys.isValid(token));
    }
}
