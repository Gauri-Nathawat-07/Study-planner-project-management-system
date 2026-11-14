package com.studyplanner.ui.components;

import com.studyplanner.util.ColorPalette;
import com.studyplanner.util.FontUtils;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom modern rounded button with off-screen double-buffering to eliminate shuttering and flicker.
 */
public class RoundedButton extends DoubleBufferedCanvas {
    private String text;
    private Color bgColor;
    private Color hoverColor;
    private Color fgColor;
    private int radius;
    private boolean isHovered = false;
    private boolean isPressed = false;
    private boolean isFocused = false;
    private boolean isEnabled = true;
    private final List<ActionListener> actionListeners = new ArrayList<>();

    public RoundedButton(String text, Color bgColor, Color fgColor, int radius) {
        this.text = text;
        this.bgColor = bgColor;
        this.fgColor = fgColor;
        this.radius = radius;
        this.hoverColor = deriveHoverColor(bgColor);

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFocusable(true);

        addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                isFocused = true;
                repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                isFocused = false;
                repaint();
            }
        });

        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (!isEnabled) return;
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER || e.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE) {
                    isPressed = true;
                    repaint();
                }
            }

            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (!isEnabled) return;
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER || e.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE) {
                    isPressed = false;
                    fireActionPerformed();
                    repaint();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isEnabled) return;
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                isPressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (!isEnabled) return;
                requestFocus();
                isPressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!isEnabled) return;
                if (isHovered && isPressed) {
                    fireActionPerformed();
                }
                isPressed = false;
                repaint();
            }
        });
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.isEnabled = enabled;
        setCursor(enabled ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
        repaint();
    }

    public boolean isButtonEnabled() {
        return isEnabled;
    }

    public RoundedButton(String text, Color bgColor, Color fgColor) {
        this(text, bgColor, fgColor, 10);
    }

    public RoundedButton(String text) {
        this(text, ColorPalette.PRIMARY, Color.WHITE, 10);
    }

    private Color deriveHoverColor(Color c) {
        if (c.equals(Color.WHITE)) return new Color(0xF1, 0xF5, 0xF9);
        int r = Math.max(0, c.getRed() - 20);
        int g = Math.max(0, c.getGreen() - 20);
        int b = Math.max(0, c.getBlue() - 20);
        return new Color(r, g, b);
    }

    public void addActionListener(ActionListener l) {
        if (l != null && !actionListeners.contains(l)) {
            actionListeners.add(l);
        }
    }

    private void fireActionPerformed() {
        ActionEvent evt = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, text);
        for (ActionListener l : actionListeners) {
            l.actionPerformed(evt);
        }
    }

    public void setText(String text) {
        this.text = text;
        repaint();
    }

    public void setBgColor(Color color) {
        this.bgColor = color;
        this.hoverColor = deriveHoverColor(color);
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(FontUtils.bold(13));
        int w = fm.stringWidth(text) + 28;
        int h = fm.getHeight() + 16;
        return new Dimension(Math.max(w, 80), Math.max(h, 36));
    }

    @Override
    protected void paintDoubleBuffered(Graphics2D g2, int w, int h) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Color activeColor = bgColor;
        if (!isEnabled) {
            activeColor = new Color(0xCB, 0xD5, 0xE1);
        } else if (isPressed) {
            activeColor = hoverColor.darker();
        } else if (isHovered) {
            activeColor = hoverColor;
        }

        // Draw pill / rounded button background
        g2.setColor(activeColor);
        g2.fillRoundRect(0, 0, w, h, radius, radius);

        // Optional border if white button
        if (bgColor.equals(Color.WHITE)) {
            g2.setColor(ColorPalette.BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);
        }

        // Focus outline ring
        if (isFocused && isEnabled) {
            g2.setColor(ColorPalette.PRIMARY);
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, radius, radius);
        }

        // Text
        g2.setFont(FontUtils.bold(13));
        g2.setColor(isEnabled ? fgColor : new Color(0x64, 0x74, 0x8B));
        FontMetrics fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(text)) / 2;
        int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(text, tx, ty);
    }
}
