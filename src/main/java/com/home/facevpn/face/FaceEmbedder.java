package com.home.facevpn.face;

import org.opencv.core.Mat;
import org.opencv.objdetect.FaceRecognizerSF;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class FaceEmbedder {

    private static final Path MODEL = Path.of("models", "face_recognition_sface_2021dec.onnx");

    private FaceRecognizerSF recognizer;

    public float[] embed(Mat frame, Mat faceRow) {
        FaceRecognizerSF recognizer = ensureRecognizer();
        Mat aligned = new Mat();
        recognizer.alignCrop(frame, faceRow, aligned);
        Mat feature = new Mat();
        recognizer.feature(aligned, feature);
        float[] embedding = new float[(int) feature.total() * feature.channels()];
        feature.get(0, 0, embedding);
        return embedding;
    }

    private FaceRecognizerSF ensureRecognizer() {
        if (recognizer == null) {
            if (!Files.exists(MODEL)) {
                throw new IllegalStateException("Model not found: " + MODEL.toAbsolutePath());
            }
            recognizer = FaceRecognizerSF.create(MODEL.toString(), "");
        }
        return recognizer;
    }
}