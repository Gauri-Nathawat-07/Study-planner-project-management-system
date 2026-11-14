package com.studyplanner.ui.components;

import com.studyplanner.util.ColorPalette;
import com.studyplanner.util.FontUtils;

import java.awt.*;

/**
 * Custom progress bar canvas displaying completion percentages with smooth rounded corners and double-buffering.
 */
public class ProgressBar extends DoubleBufferedCanvas {
    private int percentage = 0; // 0 to 100
    private Color fillColor = ColorPalette.PRIMARY;
    private Color trackColor = new Color(0xEE, 0xF2, 0xF6);
    private boolean showLabel = true;

    public ProgressBar(int percentage, Color fillColor) {
        this.percentage = Math.max(0, Math.min(100, percentage));
        if (fillColor != null) this.fillColor = fillColor;
    }

    public ProgressBar() {
        this(0, ColorPalette.PRIMARY);
    }

    public void setPercentage(int p) {
        this.percentage = Math.max(0, Math.min(100, p));
        repaint();
    }

    public int getPercentage() {
        return percentage;
    }

    public void setFillColor(Color c) {
        this.fillColor = c;
        repaint();
    }

    public void setShowLabel(boolean show) {
        this.showLabel = show;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(120, showLabel ? 20 : 10);
    }

    @Override
    protected void paintDoubleBuffered(Graphics2D g2, int w, int h) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int barH = 8;
        int barY = (h - barH) / 2;

        int trackW = showLabel ? w - 42 : w;
        if (trackW < 10) trackW = 10;

        // Track
        g2.setColor(trackColor);
        g2.fillRoundRect(0, barY, trackW, barH, barH, barH);

        // Fill
        int fillW = (int) Math.round((trackW * percentage) / 100.0);
        if (fillW > 0) {
            g2.setColor(fillColor);
            g2.fillRoundRect(0, barY, fillW, barH, barH, barH);
        }

        // Percentage Text
        if (showLabel) {
            g2.setFont(FontUtils.bold(11));
            g2.setColor(ColorPalette.TEXT_SECONDARY);
            String txt = percentage + "%";
            g2.drawString(txt, trackW + 8, barY + barH - 1);
        }
    }
}
