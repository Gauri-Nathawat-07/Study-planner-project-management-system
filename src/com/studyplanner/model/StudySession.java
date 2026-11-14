package com.studyplanner.model;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Model representing a logged study or Pomodoro focus session.
 */
public class StudySession implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String taskId;
    private String subjectId;
    private Date startTime;
    private Date endTime;
    private int durationMinutes;
    private String sessionType; // "FOCUS", "SHORT_BREAK", "LONG_BREAK"
    private String notes;

    public StudySession() {
        this.id = UUID.randomUUID().toString();
        this.sessionType = "FOCUS";
        this.startTime = new Date();
    }

    public StudySession(String taskId, String subjectId, Date startTime, Date endTime,
                        int durationMinutes, String sessionType, String notes) {
        this();
        this.taskId = taskId;
        this.subjectId = subjectId;
        this.startTime = startTime != null ? startTime : new Date();
        this.endTime = endTime != null ? endTime : new Date();
        this.durationMinutes = durationMinutes;
        this.sessionType = sessionType != null ? sessionType : "FOCUS";
        this.notes = notes;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }

    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getSessionType() { return sessionType != null ? sessionType : "FOCUS"; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }

    public String getNotes() { return notes != null ? notes : ""; }
    public void setNotes(String notes) { this.notes = notes; }
}
