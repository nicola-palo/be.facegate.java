package com.home.facevpn.camera;

import org.opencv.core.Mat;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.stereotype.Component;

import org.opencv.imgcodecs.Imgcodecs;

@Component
public class CameraCommands {

    private final CameraService camera;

    public CameraCommands(CameraService camera) {
        this.camera = camera;
    }

    @Command(name = "snapshot", description = "Capture one frame from the webcam and save it to frame.png")
    public String snapshot() {
        camera.open(0);
        try {
            Mat frame = camera.grabFrame();
            Imgcodecs.imwrite("frame.png", frame);
            return "Frame saved to frame.png";
        } finally {
            camera.close();
        }
    }
}