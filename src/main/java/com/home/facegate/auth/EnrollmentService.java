package com.home.facegate.auth;

import com.home.facegate.face.FaceEmbedder;
import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

@Component
public class EnrollmentService {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentService.class);

    private static final Path DATA_DIR = Path.of(System.getProperty("user.home"), ".facegate");
    private static final Path KEY_FILE = DATA_DIR.resolve("key.bin");
    private static final Path TEMPLATE_FILE = DATA_DIR.resolve("template.bin");
    private static final int IV_LENGTH = 12;
    private static final int TAG_BITS = 128;
    public static final double DEFAULT_THRESHOLD = 0.6;

    private final FaceEmbedder faceEmbedder;
    private final SecureRandom random = new SecureRandom();

    public EnrollmentService(FaceEmbedder faceEmbedder) {
        this.faceEmbedder = faceEmbedder;
    }

    public boolean isEnrolled() {
        return Files.exists(TEMPLATE_FILE);
    }

    public void enroll(Mat frame, Mat faceRow) {
        float[] embedding = faceEmbedder.embed(frame, faceRow);
        saveEncrypted(embedding);
    }

    public double similarity(float[] candidate) {
        return FaceAuthenticator.cosineSimilarity(loadEnrolled(), candidate);
    }

    public FaceAuthenticator authenticator() {
        return new FaceAuthenticator(loadEnrolled(), DEFAULT_THRESHOLD);
    }

    private void saveEncrypted(float[] embedding) {
        try {
            Files.createDirectories(DATA_DIR);
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key(), new GCMParameterSpec(TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(toBytes(embedding));
            ByteBuffer blob = ByteBuffer.allocate(iv.length + encrypted.length);
            blob.put(iv).put(encrypted);
            Files.write(TEMPLATE_FILE, blob.array());
            log.info("Enrollment template saved to {}", TEMPLATE_FILE);
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException("Unable to save enrollment template", e);
        }
    }

    private float[] loadEnrolled() {
        try {
            ByteBuffer blob = ByteBuffer.wrap(Files.readAllBytes(TEMPLATE_FILE));
            byte[] iv = new byte[IV_LENGTH];
            blob.get(iv);
            byte[] encrypted = new byte[blob.remaining()];
            blob.get(encrypted);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(TAG_BITS, iv));
            return fromBytes(cipher.doFinal(encrypted));
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException("Unable to read enrollment template", e);
        }
    }

    private SecretKeySpec key() throws IOException, GeneralSecurityException {
        if (Files.exists(KEY_FILE)) {
            return new SecretKeySpec(Files.readAllBytes(KEY_FILE), "AES");
        }
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(256);
        byte[] key = generator.generateKey().getEncoded();
        Files.createFile(KEY_FILE, PosixFilePermissions.asFileAttribute(
                PosixFilePermissions.fromString("rw-------")));
        Files.write(KEY_FILE, key);
        return new SecretKeySpec(key, "AES");
    }

    private static byte[] toBytes(float[] vector) {
        ByteBuffer buf = ByteBuffer.allocate(vector.length * Float.BYTES);
        for (float v : vector) {
            buf.putFloat(v);
        }
        return buf.array();
    }

    private static float[] fromBytes(byte[] bytes) {
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        float[] vector = new float[bytes.length / Float.BYTES];
        for (int i = 0; i < vector.length; i++) {
            vector[i] = buf.getFloat();
        }
        return vector;
    }
}