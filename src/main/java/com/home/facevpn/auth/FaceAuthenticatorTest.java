package com.home.facevpn.auth;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FaceAuthenticatorTest {

    private final float[] enrolled = {1f, 0f, 2f, -1f};

    @Test
    void identicalVectorsMatch() {
        assertTrue(new FaceAuthenticator(enrolled, 0.9).matches(new float[]{1f, 0f, 2f, -1f}));
    }

    @Test
    void differentVectorsDoNotMatch() {
        assertFalse(new FaceAuthenticator(enrolled, 0.9).matches(new float[]{-1f, 0f, -2f, 1f}));
    }

    @Test
    void identicalVectorsHaveSimilarityOne() {
        assertEquals(1.0, FaceAuthenticator.cosineSimilarity(enrolled, enrolled), 1e-9);
    }

    @Test
    void orthogonalVectorsHaveSimilarityZero() {
        assertEquals(0.0, FaceAuthenticator.cosineSimilarity(enrolled, new float[]{0f, 5f, 0f, 0f}), 1e-9);
    }

    @Test
    void lengthMismatchThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> FaceAuthenticator.cosineSimilarity(enrolled, new float[]{1f, 2f}));
    }
}