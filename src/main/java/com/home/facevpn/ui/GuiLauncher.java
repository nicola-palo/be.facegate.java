package com.home.facevpn.ui;

import com.home.facevpn.auth.FaceVerifier;
import com.home.facevpn.camera.CameraService;
import com.home.facevpn.vpn.VpnTunnel;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;

@Component
public class GuiLauncher implements ApplicationRunner {

    private final CameraService camera;
    private final FaceVerifier faceVerifier;
    private final VpnTunnel tunnel;
    private final Environment environment;

    public GuiLauncher(CameraService camera, FaceVerifier faceVerifier,
                       VpnTunnel tunnel, Environment environment) {
        this.camera = camera;
        this.faceVerifier = faceVerifier;
        this.tunnel = tunnel;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean interactive = environment.getProperty("spring.shell.interactive.enabled",
                Boolean.class, true);
        if (interactive && !GraphicsEnvironment.isHeadless()) {
            SwingUtilities.invokeLater(() -> new FaceVpnWindow(camera, faceVerifier, tunnel).setVisible(true));
        }
    }
}