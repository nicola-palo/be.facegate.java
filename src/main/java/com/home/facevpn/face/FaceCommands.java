package com.home.facevpn.face;

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

    public FaceCommands(CameraService camera, FaceDetector faceDetector) {
        this.camera = camera;
        this.faceDetector = faceDetector;
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
}