package com.studyplanner.util;

import java.awt.Color;

/**
 * Modern design system palette for StudyPlanner Pro.
 * Provides clean, cohesive colors for both Light and Dark themes.
 */
public class ColorPalette {
    // Light Theme Colors
    public static final Color BG = new Color(0xF4, 0xF7, 0xFB); // Light blue-grey background
    public static final Color SIDEBAR = new Color(0x17, 0x25, 0x54); // Dark navy
    public static final Color SIDEBAR_HOVER = new Color(0x1E, 0x3A, 0x8A); // Slate blue hover
    public static final Color SIDEBAR_ACTIVE = new Color(0x3B, 0x82, 0xF6); // Active highlight
    public static final Color SIDEBAR_TEXT = new Color(0x94, 0xA3, 0xB8); // Muted slate text
    public static final Color SIDEBAR_TEXT_ACTIVE = Color.WHITE;

    public static final Color PRIMARY = new Color(0x3B, 0x82, 0xF6); // Vibrant blue
    public static final Color PRIMARY_HOVER = new Color(0x25, 0x63, 0xEB); // Darker blue
    public static final Color SECONDARY = new Color(0x8B, 0x5C, 0xF6); // Vibrant purple
    public static final Color SUCCESS = new Color(0x10, 0xB9, 0x81); // Emerald green
    public static final Color WARNING = new Color(0xF5, 0x9E, 0x0B); // Amber
    public static final Color DANGER = new Color(0xEF, 0x44, 0x44); // Red
    public static final Color INFO = new Color(0x06, 0xB6, 0xD4); // Cyan

    public static final Color TEXT_PRIMARY = new Color(0x17, 0x20, 0x33); // Dark navy text
    public static final Color TEXT_SECONDARY = new Color(0x64, 0x74, 0x8B); // Muted grey text
    public static final Color TEXT_MUTED = new Color(0x94, 0xA3, 0xB8); // Light muted text

    public static final Color CARD_BG = Color.WHITE; // Pure white cards
    public static final Color BORDER = new Color(0xE2, 0xE8, 0xF0); // Soft grey border
    public static final Color CARD_HOVER = new Color(0xF8, 0xFA, 0xFC);
    public static final Color INPUT_BG = Color.WHITE;

    // Dark Theme Colors
    public static final Color DARK_BG = new Color(0x0F, 0x17, 0x2A); // Very dark slate
    public static final Color DARK_CARD = new Color(0x1E, 0x29, 0x3B); // Dark slate card
    public static final Color DARK_BORDER = new Color(0x33, 0x41, 0x55); // Dark border
    public static final Color DARK_TEXT_PRIMARY = new Color(0xF8, 0xFA, 0xFC);
    public static final Color DARK_TEXT_SECONDARY = new Color(0x94, 0xA3, 0xB8);
    public static final Color DARK_INPUT_BG = new Color(0x1E, 0x29, 0x3B);

    // Accent Subject Preset Colors
    public static final Color[] PRESET_COLORS = {
        new Color(0x3B, 0x82, 0xF6), // Blue
        new Color(0x8B, 0x5C, 0xF6), // Purple
        new Color(0x10, 0xB9, 0x81), // Green
        new Color(0xF5, 0x9E, 0x0B), // Amber
        new Color(0xEF, 0x44, 0x44), // Red
        new Color(0xEC, 0x48, 0x99), // Pink
        new Color(0x06, 0xB6, 0xD4), // Cyan
        new Color(0x63, 0x66, 0xF1)  // Indigo
    };

    public static String toHex(Color color) {
        if (color == null) return "#3B82F6";
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static Color fromHex(String hex, Color defaultColor) {
        if (hex == null || hex.trim().isEmpty()) return defaultColor;
        try {
            String clean = hex.trim();
            if (clean.startsWith("#")) clean = clean.substring(1);
            if (clean.length() == 6) {
                int r = Integer.parseInt(clean.substring(0, 2), 16);
                int g = Integer.parseInt(clean.substring(2, 4), 16);
                int b = Integer.parseInt(clean.substring(4, 6), 16);
                return new Color(r, g, b);
            }
        } catch (Exception ignored) {}
        return defaultColor;
    }
}
