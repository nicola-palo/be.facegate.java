package com.home.facegate.auth;

public class FaceAuthenticator {

    private final float[] enrolledEmbedding;
    private final double threshold;

    public FaceAuthenticator(float[] enrolledEmbedding, double threshold) {
        this.enrolledEmbedding = enrolledEmbedding;
        this.threshold = threshold;
    }

    public boolean matches(float[] candidate) {
        return cosineSimilarity(enrolledEmbedding, candidate) >= threshold;
    }

    public static double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Embedding length mismatch: " + a.length + " vs " + b.length);
        }
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += (double) a[i] * a[i];
            normB += (double) b[i] * b[i];
        }
        if (normA == 0 || normB == 0) {
            return 0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}