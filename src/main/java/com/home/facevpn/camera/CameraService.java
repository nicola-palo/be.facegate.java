package com.home.facevpn.camera;

import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.springframework.stereotype.Component;

@Component
public class CameraService {

    private VideoCapture camera;

    public CameraService() {
        OpenCV.loadShared();
    }

    public void open(int deviceIndex) {
        camera = new VideoCapture(deviceIndex);
        if (!camera.isOpened()) {
            throw new IllegalStateException("Impossible open camera " + deviceIndex);
        }
    }

    public Mat grabFrame() {
        if (camera == null || !camera.isOpened()) {
            throw new IllegalStateException("Camera not open");
        }
        Mat frame = new Mat();
        if (!camera.read(frame) || frame.empty()) {
            throw new IllegalStateException("Frame not available");
        }
        return frame;
    }

    public void close() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }
}