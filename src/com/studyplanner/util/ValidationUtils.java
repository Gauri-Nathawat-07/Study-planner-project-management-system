package com.studyplanner.util;

/**
 * Common input validation and safety checks.
 */
public class ValidationUtils {
    public static boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    public static boolean isValidInteger(String text, int min, int max) {
        if (isBlank(text)) return false;
        try {
            int val = Integer.parseInt(text.trim());
            return val >= min && val <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static int parsePositiveInt(String text, int defaultValue) {
        if (isBlank(text)) return defaultValue;
        try {
            int val = Integer.parseInt(text.trim());
            return val > 0 ? val : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
