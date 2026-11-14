package com.studyplanner.ui.components;

import com.studyplanner.app.AppConfig;
import com.studyplanner.util.ColorPalette;
import com.studyplanner.util.FontUtils;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern Dark Navy fixed navigation sidebar with double-buffered rendering to prevent shuttering.
 */
public class SidebarPanel extends Panel {
    public interface NavigationListener {
        void onNavigate(String pageKey);
    }

    public static class NavItem {
        final String key;
        final String label;
        final String icon;

        public NavItem(String key, String label, String icon) {
            this.key = key;
            this.label = label;
            this.icon = icon;
        }
    }

    private final List<NavItem> items = new ArrayList<>();
    private String activeKey = "dashboard";
    private int hoveredIndex = -1;
    private NavigationListener listener;

    private BufferedImage buffer;
    private int bufferW, bufferH;

    public SidebarPanel(NavigationListener listener) {
        this.listener = listener;
        setBackground(ColorPalette.SIDEBAR);
        setPreferredSize(new Dimension(AppConfig.SIDEBAR_WIDTH, 700));

        // Navigation Items
        items.add(new NavItem("dashboard", "Dashboard", "[D]"));
        items.add(new NavItem("tasks", "Tasks", "[T]"));
        items.add(new NavItem("projects", "Projects", "[P]"));
        items.add(new NavItem("subjects", "Subjects", "[S]"));
        items.add(new NavItem("calendar", "Calendar", "[C]"));
        items.add(new NavItem("focus", "Focus Mode", "[F]"));
        items.add(new NavItem("analytics", "Analytics", "[A]"));
        items.add(new NavItem("settings", "Settings", "[*]"));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int idx = getItemIndexAt(e.getY());
                if (idx >= 0 && idx < items.size()) {
                    String clickedKey = items.get(idx).key;
                    if (!clickedKey.equals(activeKey)) {
                        setActiveKey(clickedKey);
                        if (SidebarPanel.this.listener != null) {
                            SidebarPanel.this.listener.onNavigate(activeKey);
                        }
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (hoveredIndex != -1) {
                    hoveredIndex = -1;
                    repaint();
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int old = hoveredIndex;
                hoveredIndex = getItemIndexAt(e.getY());
                if (old != hoveredIndex) {
                    repaint();
                }
            }
        });
    }

    public void setActiveKey(String key) {
        if (!this.activeKey.equals(key)) {
            this.activeKey = key;
            repaint();
        }
    }

    public String getActiveKey() {
        return activeKey;
    }

    private int getItemIndexAt(int y) {
        int startY = 90;
        int itemH = 46;
        if (y < startY) return -1;
        int idx = (y - startY) / itemH;
        return (idx >= 0 && idx < items.size()) ? idx : -1;
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

            // Background
            g2.setColor(ColorPalette.SIDEBAR);
            g2.fillRect(0, 0, w, h);

            // Header / Brand Logo
            g2.setColor(ColorPalette.PRIMARY);
            g2.fillRoundRect(18, 20, 36, 36, 10, 10);
            g2.setColor(Color.WHITE);
            g2.setFont(FontUtils.bold(18));
            g2.drawString("SP", 25, 45);

            g2.setColor(Color.WHITE);
            g2.setFont(FontUtils.bold(16));
            g2.drawString("StudyPlanner", 64, 38);

            g2.setFont(FontUtils.regular(10));
            g2.setColor(ColorPalette.SIDEBAR_TEXT);
            g2.drawString("PRO EDITION", 64, 52);

            // Divider
            g2.setColor(new Color(0x23, 0x33, 0x68));
            g2.drawLine(18, 74, w - 18, 74);

            // Navigation Items
            int startY = 90;
            int itemH = 46;

            for (int i = 0; i < items.size(); i++) {
                NavItem it = items.get(i);
                int y = startY + i * itemH;
                boolean isActive = it.key.equals(activeKey);
                boolean isHovered = (i == hoveredIndex);

                if (isActive) {
                    g2.setColor(ColorPalette.PRIMARY);
                    g2.fillRoundRect(12, y + 2, w - 24, itemH - 4, 10, 10);
                } else if (isHovered) {
                    g2.setColor(ColorPalette.SIDEBAR_HOVER);
                    g2.fillRoundRect(12, y + 2, w - 24, itemH - 4, 10, 10);
                }

                // Icon box / symbol
                g2.setFont(FontUtils.bold(12));
                g2.setColor(isActive ? Color.WHITE : (isHovered ? Color.WHITE : ColorPalette.SIDEBAR_TEXT));
                g2.drawString(it.icon, 24, y + 27);

                // Label
                g2.setFont(isActive ? FontUtils.bold(13) : FontUtils.regular(13));
                g2.drawString(it.label, 56, y + 27);
            }

            // Bottom User Avatar
            int botY = h - 50;
            if (botY > startY + items.size() * itemH + 20) {
                g2.setColor(new Color(0x23, 0x33, 0x68));
                g2.drawLine(18, botY - 15, w - 18, botY - 15);

                g2.setColor(ColorPalette.SECONDARY);
                g2.fillOval(18, botY - 5, 30, 30);
                g2.setColor(Color.WHITE);
                g2.setFont(FontUtils.bold(13));
                g2.drawString("U", 28, botY + 15);

                g2.setFont(FontUtils.bold(12));
                g2.drawString("Logged In", 56, botY + 10);
                g2.setFont(FontUtils.regular(10));
                g2.setColor(ColorPalette.SIDEBAR_TEXT);
                g2.drawString("Local Workspace", 56, botY + 22);
            }
        } finally {
            g2.dispose();
        }

        g.drawImage(buffer, 0, 0, this);
    }
}
