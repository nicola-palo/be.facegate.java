package com.home.facevpn.auth;

import com.home.facevpn.camera.CameraService;
import com.home.facevpn.face.FaceDetector;
import com.home.facevpn.face.FaceEmbedder;
import org.opencv.core.Mat;
import org.springframework.stereotype.Component;

@Component
public class FaceVerifier {

    private final CameraService camera;
    private final FaceDetector faceDetector;
    private final FaceEmbedder faceEmbedder;
    private final EnrollmentService enrollmentService;

    public FaceVerifier(CameraService camera, FaceDetector faceDetector,
                        FaceEmbedder faceEmbedder, EnrollmentService enrollmentService) {
        this.camera = camera;
        this.faceDetector = faceDetector;
        this.faceEmbedder = faceEmbedder;
        this.enrollmentService = enrollmentService;
    }

    public Result verify() {
        if (!enrollmentService.isEnrolled()) {
            return Result.failed(0, "no enrollment — run enroll first");
        }
        camera.open(0);
        try {
            Mat frame = camera.grabFrame();
            Mat faces = faceDetector.detect(frame);
            if (faces.rows() == 0) {
                return Result.failed(0, "no face detected");
            }
            float[] candidate = faceEmbedder.embed(frame, faces.row(0));
            double similarity = enrollmentService.similarity(candidate);
            boolean ok = enrollmentService.authenticator().matches(candidate);
            return new Result(ok, similarity, ok ? "match" : "below threshold");
        } catch (RuntimeException e) {
            return Result.failed(0, e.getMessage());
        } finally {
            camera.close();
        }
    }

    public record Result(boolean authenticated, double similarity, String reason) {
        static Result failed(double similarity, String reason) {
            return new Result(false, similarity, reason);
        }
    }
}
