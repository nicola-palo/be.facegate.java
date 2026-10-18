package com.home.facegate.api;

import com.home.facegate.auth.FaceVerificationService;
import com.home.facegate.auth.TokenService;
import org.opencv.core.Mat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

@RestController
public class FaceGateController {

    private final FaceVerificationService verification;
    private final TokenService tokens;
    private final String adminToken;

    public FaceGateController(FaceVerificationService verification, TokenService tokens,
                              @Value("${facegate.admin-token}") String adminToken) {
        this.verification = verification;
        this.tokens = tokens;
        this.adminToken = adminToken;
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String landing() {
        return """
                <!DOCTYPE html>
                <html lang="it">
                <head>
                  <meta charset="utf-8">
                  <title>FaceGate</title>
                  <style>
                    body { font-family: system-ui, sans-serif; background: #0f172a; color: #e2e8f0;
                           display: grid; place-items: center; height: 100vh; margin: 0; text-align: center; }
                    code { background: #1e293b; padding: 2px 6px; border-radius: 4px; }
                  </style>
                </head>
                <body>
                  <div>
                    <h1>&#128274; FaceGate</h1>
                    <p>Area riservata: l'accesso richiede la verifica del volto.</p>
                    <p>Manda una foto a <code>POST /api/verify</code>: se il volto corrisponde
                       ricevi un token temporaneo per <code>GET /api/area-riservata</code>.</p>
                  </div>
                </body>
                </html>
                """;
    }

    @PostMapping("/api/enroll")
    public ResponseEntity<?> enroll(@RequestHeader(value = "X-Admin-Token", required = false) String admin,
                                    @RequestParam("frame") MultipartFile frame) throws IOException {
        if (!isAdmin(admin)) {
            return unauthorized("admin token required (X-Admin-Token header)");
        }
        Mat image = verification.decode(frame.getBytes());
        if (image == null) {
            return badRequest("invalid or unreadable image");
        }
        String error = verification.enroll(image);
        return error == null ? ResponseEntity.ok(Map.of("status", "enrolled")) : badRequest(error);
    }

    @PostMapping("/api/verify")
    public ResponseEntity<?> verify(@RequestParam("frame") MultipartFile frame) throws IOException {
        Mat image = verification.decode(frame.getBytes());
        if (image == null) {
            return badRequest("invalid or unreadable image");
        }
        FaceVerificationService.Result result = verification.verify(image);
        if (!result.authenticated()) {
            return ResponseEntity.status(401).body(Map.of(
                    "authenticated", false,
                    "reason", result.reason(),
                    "similarity", result.similarity()));
        }
        return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "similarity", result.similarity(),
                "token", tokens.issue("facegate-user"),
                "expiresInSeconds", tokens.ttlSeconds()));
    }

    @GetMapping("/api/area-riservata")
    public ResponseEntity<?> reservedArea(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")
                || !tokens.isValid(authorization.substring(7))) {
            return unauthorized("a valid Bearer token is required — get one via POST /api/verify");
        }
        return ResponseEntity.ok(Map.of(
                "message", "Benvenuto nell'area riservata — identita verificata",
                "subject", "facegate-user"));
    }

    private boolean isAdmin(String presented) {
        return presented != null && MessageDigest.isEqual(
                presented.getBytes(StandardCharsets.UTF_8), adminToken.getBytes(StandardCharsets.UTF_8));
    }

    private ResponseEntity<Map<String, String>> badRequest(String error) {
        return ResponseEntity.badRequest().body(Map.of("error", error));
    }

    private ResponseEntity<Map<String, String>> unauthorized(String error) {
        return ResponseEntity.status(401).body(Map.of("error", error));
    }
}
