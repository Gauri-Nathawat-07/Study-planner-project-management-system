package com.studyplanner.ui.components;

import com.studyplanner.app.AppConfig;
import com.studyplanner.util.ColorPalette;
import com.studyplanner.util.DateUtils;
import com.studyplanner.util.FontUtils;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Date;

/**
 * Top header panel displaying the current page title, live date/time, and quick action buttons.
 * Fully double-buffered to prevent shuttering.
 */
public class HeaderPanel extends Panel {
    private String pageTitle = "Dashboard";
    private RoundedButton quickActionButton;
    private final Label dateLabel;

    private BufferedImage buffer;
    private int bufferW, bufferH;

    public HeaderPanel(String defaultTitle, ActionListener quickActionHandler) {
        this.pageTitle = defaultTitle;
        setLayout(new BorderLayout());
        setBackground(ColorPalette.CARD_BG);
        setPreferredSize(new Dimension(1000, AppConfig.HEADER_HEIGHT));

        // Right container for date and button
        Panel rightPanel = new Panel(new FlowLayout(FlowLayout.RIGHT, 14, 14));
        rightPanel.setBackground(ColorPalette.CARD_BG);

        dateLabel = new Label(DateUtils.formatFull(new Date()));
        dateLabel.setFont(FontUtils.bold(12));
        dateLabel.setForeground(ColorPalette.TEXT_SECONDARY);
        rightPanel.add(dateLabel);

        if (quickActionHandler != null) {
            quickActionButton = new RoundedButton("+ New Task", ColorPalette.PRIMARY, Color.WHITE, 8);
            quickActionButton.addActionListener(quickActionHandler);
            rightPanel.add(quickActionButton);
        }

        add(rightPanel, BorderLayout.EAST);
    }

    public void setPageTitle(String title) {
        if (!this.pageTitle.equals(title)) {
            this.pageTitle = title;
            repaint();
        }
    }

    public void refreshDate() {
        dateLabel.setText(DateUtils.formatFull(new Date()));
    }

    @Override
    public void update(Graphics g) {
        paint(g);
    }

    @Override
    public void paint(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        if (buffer == null || bufferW != w || bufferH != h) {
            buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            bufferW = w;
            bufferH = h;
        }

        Graphics2D g2 = buffer.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2.setColor(ColorPalette.CARD_BG);
            g2.fillRect(0, 0, w, h);

            // Bottom border
            g2.setColor(ColorPalette.BORDER);
            g2.drawLine(0, h - 1, w, h - 1);

            // Title on the left
            g2.setFont(FontUtils.bold(20));
            g2.setColor(ColorPalette.TEXT_PRIMARY);
            g2.drawString(pageTitle, 24, 40);
        } finally {
            g2.dispose();
        }

        g.drawImage(buffer, 0, 0, this);
        super.paint(g);
    }
}
