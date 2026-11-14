package com.studyplanner.ui.components;

import com.studyplanner.util.ColorPalette;
import com.studyplanner.util.FontUtils;

import java.awt.*;

/**
 * Modern statistic card showing metric numbers, labels, and an accent indicator with double-buffering.
 */
public class StatCard extends DoubleBufferedCanvas {
    private String title;
    private String value;
    private String subtitle;
    private Color accentColor;
    private boolean isDark = false;

    public StatCard(String title, String value, String subtitle, Color accentColor) {
        this.title = title;
        this.value = value;
        this.subtitle = subtitle;
        this.accentColor = accentColor;
    }

    public void updateData(String value, String subtitle) {
        this.value = value;
        this.subtitle = subtitle;
        repaint();
    }

    public void setDarkMode(boolean dark) {
        this.isDark = dark;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(160, 95);
    }

    @Override
    protected void paintDoubleBuffered(Graphics2D g2, int w, int h) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Card background
        g2.setColor(isDark ? ColorPalette.DARK_CARD : ColorPalette.CARD_BG);
        g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);

        // Border
        g2.setColor(isDark ? ColorPalette.DARK_BORDER : ColorPalette.BORDER);
        g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);

        // Left accent indicator strip
        g2.setColor(accentColor);
        g2.fillRoundRect(0, 0, 5, h - 1, 6, 6);

        // Title
        g2.setFont(FontUtils.bold(11));
        g2.setColor(isDark ? ColorPalette.DARK_TEXT_SECONDARY : ColorPalette.TEXT_SECONDARY);
        g2.drawString(title.toUpperCase(), 18, 24);

        // Big Metric Value
        g2.setFont(FontUtils.bold(22));
        g2.setColor(isDark ? ColorPalette.DARK_TEXT_PRIMARY : ColorPalette.TEXT_PRIMARY);
        g2.drawString(value, 18, 54);

        // Subtitle / context note
        if (subtitle != null && !subtitle.isEmpty()) {
            g2.setFont(FontUtils.regular(11));
            g2.setColor(accentColor);
            g2.drawString(subtitle, 18, 76);
        }
    }
}
