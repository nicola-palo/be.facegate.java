package com.home.facegate.auth;

import com.home.facegate.face.FaceDetector;
import com.home.facegate.face.FaceEmbedder;
import com.home.facegate.face.OpenCvNative;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.stereotype.Component;

@Component
public class FaceVerificationService {

    private final FaceDetector faceDetector;
    private final FaceEmbedder faceEmbedder;
    private final EnrollmentService enrollmentService;

    public FaceVerificationService(FaceDetector faceDetector, FaceEmbedder faceEmbedder,
                                   EnrollmentService enrollmentService) {
        this.faceDetector = faceDetector;
        this.faceEmbedder = faceEmbedder;
        this.enrollmentService = enrollmentService;
    }

    // loads OpenCV natives: any Mat use must come after this
    public Mat decode(byte[] image) {
        OpenCvNative.load();
        Mat decoded = Imgcodecs.imdecode(new MatOfByte(image), Imgcodecs.IMREAD_COLOR);
        return decoded.empty() ? null : decoded;
    }

    // YuNet and SFace sessions are not thread-safe: one verification at a time
    public synchronized String enroll(Mat frame) {
        Mat faces = faceDetector.detect(frame);
        if (faces.rows() == 0) {
            return "no face detected";
        }
        if (faces.rows() > 1) {
            return "multiple faces detected — exactly one face required";
        }
        enrollmentService.enroll(frame, faces.row(0));
        return null;
    }

    public synchronized Result verify(Mat frame) {
        Mat faces = faceDetector.detect(frame);
        if (faces.rows() == 0) {
            return Result.failed(0, "no face detected");
        }
        if (!enrollmentService.isEnrolled()) {
            return Result.failed(0, "no enrollment — POST /api/enroll first");
        }
        float[] candidate = faceEmbedder.embed(frame, faces.row(0));
        double similarity = enrollmentService.similarity(candidate);
        boolean matched = enrollmentService.authenticator().matches(candidate);
        return new Result(matched, similarity, matched ? "match" : "below threshold");
    }

    public record Result(boolean authenticated, double similarity, String reason) {
        static Result failed(double similarity, String reason) {
            return new Result(false, similarity, reason);
        }
    }
}
