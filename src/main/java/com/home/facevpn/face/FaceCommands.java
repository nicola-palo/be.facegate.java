package com.home.facevpn.face;

import com.home.facevpn.auth.FaceAuthenticator;
import com.home.facevpn.camera.CameraService;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.stereotype.Component;

@Component
public class FaceCommands {

    private final CameraService camera;
    private final FaceDetector faceDetector;
    private final FaceEmbedder faceEmbedder;

    public FaceCommands(CameraService camera, FaceDetector faceDetector, FaceEmbedder faceEmbedder) {
        this.camera = camera;
        this.faceDetector = faceDetector;
        this.faceEmbedder = faceEmbedder;
    }

    @Command(name = "detect", description = "Capture a frame, detect faces and save the annotated image to faces.png")
    public String detect() {
        camera.open(0);
        try {
            Mat frame = camera.grabFrame();
            Mat faces = faceDetector.detect(frame);
            for (int i = 0; i < faces.rows(); i++) {
                double[] d = faces.get(i, 0);
                Imgproc.rectangle(frame, new Rect((int) d[0], (int) d[1], (int) d[2], (int) d[3]),
                        new Scalar(0, 255, 0), 2);
            }
            Imgcodecs.imwrite("faces.png", frame);
            return "Faces detected: " + faces.rows() + " (saved to faces.png)";
        } finally {
            camera.close();
        }
    }

    @Command(name = "similarity", description = "Capture two frames and print the cosine similarity between their face embeddings")
    public String similarity() throws InterruptedException {
        camera.open(0);
        try {
            Mat frame1 = camera.grabFrame();
            Thread.sleep(500);
            Mat frame2 = camera.grabFrame();
            Mat faces1 = faceDetector.detect(frame1);
            Mat faces2 = faceDetector.detect(frame2);
            if (faces1.rows() == 0 || faces2.rows() == 0) {
                return "No face detected in one of the two frames";
            }
            float[] e1 = faceEmbedder.embed(frame1, faces1.row(0));
            float[] e2 = faceEmbedder.embed(frame2, faces2.row(0));
            double sim = FaceAuthenticator.cosineSimilarity(e1, e2);
            return "Embedding size: " + e1.length + " | cosine similarity: " + String.format("%.3f", sim);
        } finally {
            camera.close();
        }
    }
}