package com.home.facevpn.ui;

import com.home.facevpn.auth.FaceVerifier;
import com.home.facevpn.camera.CameraService;
import com.home.facevpn.vpn.VpnTunnel;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

public class FaceVpnWindow extends JFrame {

    private static final Color GREEN = new Color(0x34C759);
    private static final Color RED = new Color(0xFF3B30);
    private static final Color AMBER = new Color(0xB25000);
    private static final Color GREY = new Color(0x787880);

    private final CameraService camera;
    private final FaceVerifier faceVerifier;
    private final VpnTunnel tunnel;

    private final CameraPanel cameraPanel = new CameraPanel();
    private final ToggleSwitch toggle = new ToggleSwitch();
    private final JLabel status = new JLabel("VPN INATTIVA", JLabel.CENTER);

    public FaceVpnWindow(CameraService camera, FaceVerifier faceVerifier, VpnTunnel tunnel) {
        super("FaceVPN");
        this.camera = camera;
        this.faceVerifier = faceVerifier;
        this.tunnel = tunnel;

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        status.setFont(status.getFont().deriveFont(Font.BOLD, 18f));
        status.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel controls = new JPanel(new BorderLayout(16, 0));
        controls.setBorder(BorderFactory.createEmptyBorder(8, 16, 12, 16));
        controls.add(toggle, BorderLayout.WEST);
        controls.add(new JLabel("Clicca per attivare/disattivare (FaceID)"), BorderLayout.CENTER);

        add(status, BorderLayout.NORTH);
        add(cameraPanel, BorderLayout.CENTER);
        add(controls, BorderLayout.SOUTH);

        toggle.addActionListener(this::onToggle);

        pack();
        setLocationRelativeTo(null);
        applyState(false, "VPN INATTIVA");
        startPreview();
    }

    private void startPreview() {
        Thread preview = new Thread(() -> {
            while (true) {
                try {
                    byte[] bmp = Imgcodecs.imencode(".bmp", camera.grabFrame()).toArray();
                    SwingUtilities.invokeLater(() -> cameraPanel.updateFrame(bmp));
                    Thread.sleep(33); // ~30 fps
                } catch (InterruptedException e) {
                    return;
                } catch (RuntimeException e) {
                    try { Thread.sleep(1000); } catch (InterruptedException ie) { return; }
                }
            }
        }, "camera-preview");
        preview.setDaemon(true);
        preview.start();
    }

    private void onToggle() {
        boolean wantOn = !tunnel.isActive();
        toggle.setEnabled(false);
        applyState(false, "VERIFICA IN CORSO...");
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                if (!wantOn) {
                    try { tunnel.down(); return "VPN INATTIVA"; }
                    catch (Exception e) { return "ERRORE: " + e.getMessage(); }
                }
                FaceVerifier.Result face = faceVerifier.verify();
                if (!face.authenticated()) {
                    return "ACCESSO NEGATO — %s (sim %.3f)".formatted(face.reason(), face.similarity());
                }
                try { tunnel.up(); return "VPN ATTIVA"; }
                catch (Exception e) { return "ERRORE: " + e.getMessage(); }
            }
            @Override
            protected void done() {
                try { applyResult(get()); }
                catch (Exception e) { applyResult("ERRORE: " + e.getMessage()); }
            }
        }.execute();
    }

    private void applyResult(String result) {
        toggle.setEnabled(true);
        if (result.startsWith("VPN ATTIVA")) {
            applyState(true, result);
        } else if (result.startsWith("ACCESSO NEGATO")) {
            applyState(false, result);
        } else if (result.equals("VPN INATTIVA")) {
            applyState(false, result);
        } else {
            status.setText(result);
            status.setBackground(RED);
        }
    }

    private void applyState(boolean active, String text) {
        toggle.setOn(active);
        status.setText(text);
        status.setBackground(active ? GREEN : "ACCESSO NEGATO".equals(text) ? RED : GREY);
        status.setForeground(Color.WHITE);
    }
}