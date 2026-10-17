package com.home.facevpn.camera;

import jakarta.annotation.PreDestroy;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.springframework.stereotype.Component;

@Component
public class CameraService {

    private static final int DEVICE_INDEX = 0;

    private VideoCapture camera;

    public CameraService() {
        OpenCV.loadShared();
    }

    public synchronized void ensureOpen() {
        if (camera == null || !camera.isOpened()) {
            camera = new VideoCapture(DEVICE_INDEX);
            if (!camera.isOpened()) {
                camera = null;
                throw new IllegalStateException("Impossible to open camera " + DEVICE_INDEX);
            }
        }
    }

    public synchronized Mat grabFrame() {
        ensureOpen();
        Mat frame = new Mat();
        if (!camera.read(frame) || frame.empty()) {
            throw new IllegalStateException("Frame not available");
        }
        return frame;
    }

    @PreDestroy
    public synchronized void shutdown() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }
}