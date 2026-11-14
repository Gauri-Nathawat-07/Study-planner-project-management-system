package com.studyplanner.model;

import java.io.Serializable;

public enum TaskStatus implements Serializable {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed");

    private static final long serialVersionUID = 1L;
    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static TaskStatus fromString(String text) {
        if (text == null) return PENDING;
        for (TaskStatus s : values()) {
            if (s.name().equalsIgnoreCase(text) || s.displayName.equalsIgnoreCase(text)) {
                return s;
            }
        }
        return PENDING;
    }
}
