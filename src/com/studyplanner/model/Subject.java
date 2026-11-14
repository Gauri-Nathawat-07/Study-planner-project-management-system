package com.studyplanner.model;

import java.io.Serializable;
import java.util.UUID;

/**
 * Model representing an academic course/subject.
 */
public class Subject implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String description;
    private String colorHex;
    private int targetWeeklyHours;
    private long totalStudyMinutes;

    public Subject() {
        this.id = UUID.randomUUID().toString();
        this.colorHex = "#3B82F6";
        this.targetWeeklyHours = 5;
        this.totalStudyMinutes = 0;
    }

    public Subject(String name, String description, String colorHex, int targetWeeklyHours) {
        this();
        this.name = name;
        this.description = description;
        this.colorHex = colorHex != null ? colorHex : "#3B82F6";
        this.targetWeeklyHours = targetWeeklyHours > 0 ? targetWeeklyHours : 5;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description != null ? description : ""; }
    public void setDescription(String description) { this.description = description; }

    public String getColorHex() { return colorHex != null ? colorHex : "#3B82F6"; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }

    public int getTargetWeeklyHours() { return targetWeeklyHours; }
    public void setTargetWeeklyHours(int targetWeeklyHours) { this.targetWeeklyHours = targetWeeklyHours; }

    public long getTotalStudyMinutes() { return totalStudyMinutes; }
    public void setTotalStudyMinutes(long totalStudyMinutes) { this.totalStudyMinutes = totalStudyMinutes; }

    public void addStudyMinutes(long minutes) {
        if (minutes > 0) {
            this.totalStudyMinutes += minutes;
        }
    }

    @Override
    public String toString() {
        return name != null ? name : "Unnamed Subject";
    }
}
