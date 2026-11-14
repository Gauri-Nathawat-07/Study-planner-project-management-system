package com.studyplanner.util;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.List;

/**
 * System font detection and typography helper.
 * Uses Segoe UI on Windows, SF Pro on Mac, or standard SansSerif fallback.
 */
public class FontUtils {
    private static final String FONT_FAMILY;

    static {
        String[] preferred = {"Segoe UI", "SF Pro Display", "Helvetica Neue", "Arial", "SansSerif"};
        List<String> available = Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        String chosen = "SansSerif";
        for (String f : preferred) {
            if (available.contains(f)) {
                chosen = f;
                break;
            }
        }
        FONT_FAMILY = chosen;
    }

    public static String getFontFamily() {
        return FONT_FAMILY;
    }

    public static Font regular(int size) {
        return new Font(FONT_FAMILY, Font.PLAIN, size);
    }

    public static Font bold(int size) {
        return new Font(FONT_FAMILY, Font.BOLD, size);
    }

    public static Font italic(int size) {
        return new Font(FONT_FAMILY, Font.ITALIC, size);
    }
}
