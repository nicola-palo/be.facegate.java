package com.home.facevpn.ui;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

public class CameraPanel extends JPanel {

    private volatile BufferedImage frame;

    public CameraPanel() {
        setPreferredSize(new Dimension(640, 480));
    }

    public void updateFrame(byte[] encodedBmp) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(encodedBmp));
            if (image != null) {
                this.frame = image;
                repaint();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        BufferedImage image = frame;
        if (image != null) {
            double scale = Math.min((double) getWidth() / image.getWidth(),
                    (double) getHeight() / image.getHeight());
            int w = (int) (image.getWidth() * scale), h = (int) (image.getHeight() * scale);
            g.drawImage(image, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, null);
        }
    }
}