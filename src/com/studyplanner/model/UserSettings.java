package com.studyplanner.model;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Model representing user preferences and configurable study parameters.
 */
public class UserSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userName;
    private int focusDurationMinutes;
    private int shortBreakDurationMinutes;
    private int longBreakDurationMinutes;
    private int weeklyStudyHourTarget;
    private int preferredWeekStartDay; // Calendar.MONDAY or Calendar.SUNDAY
    private boolean darkMode;
    private boolean confirmDeletions;

    public UserSettings() {
        this.userName = "Scholar";
        this.focusDurationMinutes = 25;
        this.shortBreakDurationMinutes = 5;
        this.longBreakDurationMinutes = 15;
        this.weeklyStudyHourTarget = 20;
        this.preferredWeekStartDay = Calendar.MONDAY;
        this.darkMode = false;
        this.confirmDeletions = true;
    }

    public String getUserName() { return userName != null ? userName : "Scholar"; }
    public void setUserName(String userName) { this.userName = userName; }

    public int getFocusDurationMinutes() { return focusDurationMinutes > 0 ? focusDurationMinutes : 25; }
    public void setFocusDurationMinutes(int focusDurationMinutes) { this.focusDurationMinutes = focusDurationMinutes; }

    public int getShortBreakDurationMinutes() { return shortBreakDurationMinutes > 0 ? shortBreakDurationMinutes : 5; }
    public void setShortBreakDurationMinutes(int shortBreakDurationMinutes) { this.shortBreakDurationMinutes = shortBreakDurationMinutes; }

    public int getLongBreakDurationMinutes() { return longBreakDurationMinutes > 0 ? longBreakDurationMinutes : 15; }
    public void setLongBreakDurationMinutes(int longBreakDurationMinutes) { this.longBreakDurationMinutes = longBreakDurationMinutes; }

    public int getWeeklyStudyHourTarget() { return weeklyStudyHourTarget > 0 ? weeklyStudyHourTarget : 20; }
    public void setWeeklyStudyHourTarget(int weeklyStudyHourTarget) { this.weeklyStudyHourTarget = weeklyStudyHourTarget; }

    public int getPreferredWeekStartDay() { return preferredWeekStartDay; }
    public void setPreferredWeekStartDay(int preferredWeekStartDay) { this.preferredWeekStartDay = preferredWeekStartDay; }

    public boolean isDarkMode() { return darkMode; }
    public void setDarkMode(boolean darkMode) { this.darkMode = darkMode; }

    public boolean isConfirmDeletions() { return confirmDeletions; }
    public void setConfirmDeletions(boolean confirmDeletions) { this.confirmDeletions = confirmDeletions; }
}
