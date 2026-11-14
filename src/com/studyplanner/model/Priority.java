package com.studyplanner.model;

import java.io.Serializable;

public enum Priority implements Serializable {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    URGENT("Urgent");

    private static final long serialVersionUID = 1L;
    private final String displayName;

    Priority(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static Priority fromString(String text) {
        if (text == null) return MEDIUM;
        for (Priority p : values()) {
            if (p.name().equalsIgnoreCase(text) || p.displayName.equalsIgnoreCase(text)) {
                return p;
            }
        }
        return MEDIUM;
    }
}
