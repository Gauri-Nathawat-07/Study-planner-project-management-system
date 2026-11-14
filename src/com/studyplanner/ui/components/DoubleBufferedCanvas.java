package com.studyplanner.ui.components;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Double-buffered canvas base class to completely eliminate flicker and shuttering in standard Java AWT.
 */
public abstract class DoubleBufferedCanvas extends Canvas {
    private BufferedImage offscreenImage;
    private int offscreenWidth;
    private int offscreenHeight;

    @Override
    public void update(Graphics g) {
        paint(g);
    }

    @Override
    public void paint(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        // Check if buffer needs regeneration
        if (offscreenImage == null || offscreenWidth != w || offscreenHeight != h) {
            offscreenImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            offscreenWidth = w;
            offscreenHeight = h;
        }

        Graphics2D offG2 = offscreenImage.createGraphics();
        try {
            // Clear buffer with background
            Color bg = getBackground();
            if (bg != null) {
                offG2.setColor(bg);
                offG2.fillRect(0, 0, w, h);
            }

            paintDoubleBuffered(offG2, w, h);
        } finally {
            offG2.dispose();
        }

        // Blit offscreen buffer in a single atomic draw call
        g.drawImage(offscreenImage, 0, 0, this);
    }

    /**
     * Subclasses implement their custom painting into the offscreen graphics buffer.
     */
    protected abstract void paintDoubleBuffered(Graphics2D g2, int width, int height);
}

