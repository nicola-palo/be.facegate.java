package com.home.facevpn.auth;

import com.home.facevpn.camera.CameraService;
import org.opencv.core.Mat;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.stereotype.Component;
import com.home.facevpn.face.FaceDetector;
import com.home.facevpn.face.FaceEmbedder;

@Component
public class EnrollmentCommands {

    private final CameraService camera;
    private final FaceDetector faceDetector;
    private final FaceEmbedder faceEmbedder;
    private final EnrollmentService enrollmentService;

    public EnrollmentCommands(CameraService camera, FaceDetector faceDetector,
                              FaceEmbedder faceEmbedder, EnrollmentService enrollmentService) {
        this.camera = camera;
        this.faceDetector = faceDetector;
        this.faceEmbedder = faceEmbedder;
        this.enrollmentService = enrollmentService;
    }

    @Command(name = "enroll", description = "Register your face as the VPN owner template (encrypted)")
    public String enroll() {
        camera.open(0);
        try {
            Mat frame = camera.grabFrame();
            Mat faces = faceDetector.detect(frame);
            if (faces.rows() == 0) {
                return "No face detected, try again";
            }
            if (faces.rows() > 1) {
                return "Multiple faces detected — be alone in front of the camera";
            }
            enrollmentService.enroll(frame, faces.row(0));
            return "Face enrolled: template saved (encrypted) to ~/.facevpn";
        } finally {
            camera.close();
        }
    }

    @Command(name = "verify", description = "Compare the face in front of the camera with the enrolled template")
    public String verify() {
        if (!enrollmentService.isEnrolled()) {
            return "No enrollment yet — run enroll first";
        }
        camera.open(0);
        try {
            Mat frame = camera.grabFrame();
            Mat faces = faceDetector.detect(frame);
            if (faces.rows() == 0) {
                return "No face detected";
            }
            float[] candidate = faceEmbedder.embed(frame, faces.row(0));
            double sim = enrollmentService.similarity(candidate);
            boolean match = enrollmentService.authenticator().matches(candidate);
            return String.format("Similarity: %.3f -> %s", sim, match ? "MATCH" : "NO MATCH");
        } finally {
            camera.close();
        }
    }
}