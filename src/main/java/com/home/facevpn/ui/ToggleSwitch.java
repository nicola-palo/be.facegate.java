package com.home.facevpn.ui;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class ToggleSwitch extends JComponent {

    private static final Color ON = new Color(0x34C759);
    private static final Color OFF = new Color(0x787880);

    private final List<Runnable> listeners = new ArrayList<>();
    private boolean on;

    public ToggleSwitch() {
        setPreferredSize(new Dimension(120, 56));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                listeners.forEach(Runnable::run);
            }
        });
    }

    public void addActionListener(Runnable listener) {
        listeners.add(listener);
    }

    public void setOn(boolean on) {
        this.on = on;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int h = getHeight();
        g2.setColor(on ? ON : OFF);
        g2.fillRoundRect(0, 0, getWidth(), h, h, h);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2f));
        int knob = h - 8;
        int x = on ? getWidth() - knob - 4 : 4;
        g2.fillOval(x, 4, knob, knob);
    }
}