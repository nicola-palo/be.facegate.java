package com.home.facevpn.face;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.objdetect.FaceDetectorYN;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class FaceDetector {

    private static final Path MODEL = Path.of("models", "face_detection_yunet_2023mar.onnx");
    private static final float SCORE_THRESHOLD = 0.6f;

    private FaceDetectorYN detector;

    public Mat detect(Mat frame) {
        FaceDetectorYN detector = ensureDetector();
        detector.setInputSize(frame.size());
        Mat faces = new Mat();
        detector.detect(frame, faces);
        return faces;
    }

    private FaceDetectorYN ensureDetector() {
        if (detector == null) {
            if (!Files.exists(MODEL)) {
                throw new IllegalStateException("Model not found: " + MODEL.toAbsolutePath());
            }
            detector = FaceDetectorYN.create(MODEL.toString(), "", new Size(320, 320),
                    SCORE_THRESHOLD, 0.3f, 5000);
        }
        return detector;
    }
}