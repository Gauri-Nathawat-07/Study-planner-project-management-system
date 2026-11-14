package com.studyplanner.model;

import java.io.Serializable;

public enum ProjectStatus implements Serializable {
    NOT_STARTED("Not Started"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    ON_HOLD("On Hold");

    private static final long serialVersionUID = 1L;
    private final String displayName;

    ProjectStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static ProjectStatus fromString(String text) {
        if (text == null) return NOT_STARTED;
        for (ProjectStatus s : values()) {
            if (s.name().equalsIgnoreCase(text) || s.displayName.equalsIgnoreCase(text)) {
                return s;
            }
        }
        return NOT_STARTED;
    }
}
